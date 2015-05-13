// Copyright 2012 Google Inc. All Rights Reserved.
//
// Use of this source code is governed by a BSD-style license
// that can be found in the COPYING file in the root of the source
// tree. An additional intellectual property rights grant can be found
// in the file PATENTS. All contributing project authors may
// be found in the AUTHORS file in the root of the source tree.
// -----------------------------------------------------------------------------
//
// Author: Jyrki Alakuijala (jyrki@google.com)
//
#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <math.h>

#include "./backward_references.h"
#include "./histogram.h"
#include "../dsp/lossless.h"
#include "../utils/utils.h"

#define MAX_COST 1.e38

// Number of partitions for the three dominant (literal, red and blue) symbol
// costs.
#define NUM_PARTITIONS 4
// The size of the bin-hash corresponding to the three dominant costs.
#define BIN_SIZE (NUM_PARTITIONS * NUM_PARTITIONS * NUM_PARTITIONS)

static void HistogramClear(VP8LHistogram* const p) {
  memset(p->literal_, 0, sizeof(p->literal_));
  memset(p->red_, 0, sizeof(p->red_));
  memset(p->blue_, 0, sizeof(p->blue_));
  memset(p->alpha_, 0, sizeof(p->alpha_));
  memset(p->distance_, 0, sizeof(p->distance_));
  p->bit_cost_ = 0;
}

void VP8LHistogramStoreRefs(const VP8LBackwardRefs* const refs,
                            VP8LHistogram* const histo) {
  int i;
  for (i = 0; i < refs->size; ++i) {
    VP8LHistogramAddSinglePixOrCopy(histo, &refs->refs[i]);
  }
}

void VP8LHistogramCreate(VP8LHistogram* const p,
                         const VP8LBackwardRefs* const refs,
                         int palette_code_bits) {
  if (palette_code_bits >= 0) {
    p->palette_code_bits_ = palette_code_bits;
  }
  HistogramClear(p);
  VP8LHistogramStoreRefs(refs, p);
}

void VP8LHistogramInit(VP8LHistogram* const p, int palette_code_bits) {
  p->palette_code_bits_ = palette_code_bits;
  HistogramClear(p);
}

VP8LHistogramSet* VP8LAllocateHistogramSet(int size, int cache_bits) {
  int i;
  VP8LHistogramSet* set;
  VP8LHistogram* bulk;
  const uint64_t total_size = sizeof(*set)
                            + (uint64_t)size * sizeof(*set->histograms)
                            + (uint64_t)size * sizeof(**set->histograms);
  uint8_t* memory = (uint8_t*)WebPSafeMalloc(total_size, sizeof(*memory));
  if (memory == NULL) return NULL;

  set = (VP8LHistogramSet*)memory;
  memory += sizeof(*set);
  set->histograms = (VP8LHistogram**)memory;
  memory += size * sizeof(*set->histograms);
  bulk = (VP8LHistogram*)memory;
  set->max_size = size;
  set->size = size;
  for (i = 0; i < size; ++i) {
    set->histograms[i] = bulk + i;
    VP8LHistogramInit(set->histograms[i], cache_bits);
  }
  return set;
}

// -----------------------------------------------------------------------------

void VP8LHistogramAddSinglePixOrCopy(VP8LHistogram* const histo,
                                     const PixOrCopy* const v) {
  if (PixOrCopyIsLiteral(v)) {
    ++histo->alpha_[PixOrCopyLiteral(v, 3)];
    ++histo->red_[PixOrCopyLiteral(v, 2)];
    ++histo->literal_[PixOrCopyLiteral(v, 1)];
    ++histo->blue_[PixOrCopyLiteral(v, 0)];
  } else if (PixOrCopyIsCacheIdx(v)) {
    int literal_ix = 256 + NUM_LENGTH_CODES + PixOrCopyCacheIdx(v);
    ++histo->literal_[literal_ix];
  } else {
    int code, extra_bits;
    VP8LPrefixEncodeBits(PixOrCopyLength(v), &code, &extra_bits);
    ++histo->literal_[256 + code];
    VP8LPrefixEncodeBits(PixOrCopyDistance(v), &code, &extra_bits);
    ++histo->distance_[code];
  }
}

static WEBP_INLINE double BitsEntropyRefine(int nonzeros, int sum, int max_val,
                                            double retval) {
  double mix;
  if (nonzeros < 5) {
    if (nonzeros <= 1) {
      return 0;
    }
    // Two symbols, they will be 0 and 1 in a Huffman code.
    // Let's mix in a bit of entropy to favor good clustering when
    // distributions of these are combined.
    if (nonzeros == 2) {
      return 0.99 * sum + 0.01 * retval;
    }
    // No matter what the entropy says, we cannot be better than min_limit
    // with Huffman coding. I am mixing a bit of entropy into the
    // min_limit since it produces much better (~0.5 %) compression results
    // perhaps because of better entropy clustering.
    if (nonzeros == 3) {
      mix = 0.95;
    } else {
      mix = 0.7;  // nonzeros == 4.
    }
  } else {
    mix = 0.627;
  }

  {
    double min_limit = 2 * sum - max_val;
    min_limit = mix * min_limit + (1.0 - mix) * retval;
    return (retval < min_limit) ? min_limit : retval;
  }
}

static double BitsEntropy(const int* const array, int n) {
  double retval = 0.;
  int sum = 0;
  int nonzeros = 0;
  int max_val = 0;
  int i;
  for (i = 0; i < n; ++i) {
    if (array[i] != 0) {
      sum += array[i];
      ++nonzeros;
      retval -= VP8LFastSLog2(array[i]);
      if (max_val < array[i]) {
        max_val = array[i];
      }
    }
  }
  retval += VP8LFastSLog2(sum);
  return BitsEntropyRefine(nonzeros, sum, max_val, retval);
}

static double BitsEntropyCombined(const int* const X, const int* const Y,
                                  int n) {
  double retval = 0.;
  int sum = 0;
  int nonzeros = 0;
  int max_val = 0;
  int i;
  for (i = 0; i < n; ++i) {
    const int xy = X[i] + Y[i];
    if (xy != 0) {
      sum += xy;
      ++nonzeros;
      retval -= VP8LFastSLog2(xy);
      if (max_val < xy) {
        max_val = xy;
      }
    }
  }
  retval += VP8LFastSLog2(sum);
  return BitsEntropyRefine(nonzeros, sum, max_val, retval);
}

static WEBP_INLINE double InitialHuffmanCost(void) {
  // Small bias because Huffman code length is typically not stored in
  // full length.
  static const int kHuffmanCodeOfHuffmanCodeSize = CODE_LENGTH_CODES * 3;
  static const double kSmallBias = 9.1;
  return kHuffmanCodeOfHuffmanCodeSize - kSmallBias;
}

static WEBP_INLINE double HuffmanCostRefine(int streak, int val) {
  double retval;
  if (streak > 3) {
    if (val == 0) {
      retval = 1.5625 + 0.234375 * streak;
    } else {
      retval = 2.578125 + 0.703125 * streak;
    }
  } else {
    if (val == 0) {
      retval = 1.796875 * streak;
    } else {
      retval = 3.28125 * streak;
    }
  }
  return retval;
}

// Returns the cost encode the rle-encoded entropy code.
// The constants in this function are experimental.
static double HuffmanCost(const int* const population, int length) {
  int streak = 0;
  int i = 0;
  double retval = InitialHuffmanCost();
  for (; i < length - 1; ++i) {
    ++streak;
    if (population[i] == population[i + 1]) {
      continue;
    }
    retval += HuffmanCostRefine(streak, population[i]);
    streak = 0;
  }
  retval += HuffmanCostRefine(++streak, population[i]);
  return retval;
}

static double HuffmanCostCombined(const int* const X, const int* const Y,
                                  int length) {
  int streak = 0;
  int i = 0;
  double retval = InitialHuffmanCost();
  for (; i < length - 1; ++i) {
    const int xy = X[i] + Y[i];
    const int xy_next = X[i + 1] + Y[i + 1];
    ++streak;
    if (xy == xy_next) {
      continue;
    }
    retval += HuffmanCostRefine(streak, xy);
    streak = 0;
  }
  retval += HuffmanCostRefine(++streak, X[i] + Y[i]);
  return retval;
}

static double PopulationCost(const int* const population, int length) {
  return BitsEntropy(population, length) + HuffmanCost(population, length);
}

static double GetCombinedEntropy(const int* const X, const int* const Y,
                                 int length) {
  return BitsEntropyCombined(X, Y, length) + HuffmanCostCombined(X, Y, length);
}

static double ExtraCost(const int* const population, int length) {
  int i;
  double cost = 0.;
  for (i = 2; i < length - 2; ++i) cost += (i >> 1) * population[i + 2];
  return cost;
}

static double ExtraCostCombined(const int* const X, const int* const Y,
                                int length) {
  int i;
  double cost = 0.;
  for (i = 2; i < length - 2; ++i) {
    const int xy = X[i + 2] + Y[i + 2];
    cost += (i >> 1) * xy;
  }
  return cost;
}

// Estimates the Entropy + Huffman + other block overhead size cost.
double VP8LHistogramEstimateBits(const VP8LHistogram* const p) {
  return
      PopulationCost(p->literal_, VP8LHistogramNumCodes(p->palette_code_bits_))
      + PopulationCost(p->red_, 256)
      + PopulationCost(p->blue_, 256)
      + PopulationCost(p->alpha_, 256)
      + PopulationCost(p->distance_, NUM_DISTANCE_CODES)
      + ExtraCost(p->literal_ + 256, NUM_LENGTH_CODES)
      + ExtraCost(p->distance_, NUM_DISTANCE_CODES);
}

double VP8LHistogramEstimateBitsBulk(const VP8LHistogram* const p) {
  return
      BitsEntropy(p->literal_, VP8LHistogramNumCodes(p->palette_code_bits_))
      + BitsEntropy(p->red_, 256)
      + BitsEntropy(p->blue_, 256)
      + BitsEntropy(p->alpha_, 256)
      + BitsEntropy(p->distance_, NUM_DISTANCE_CODES)
      + ExtraCost(p->literal_ + 256, NUM_LENGTH_CODES)
      + ExtraCost(p->distance_, NUM_DISTANCE_CODES);
}

// -----------------------------------------------------------------------------
// Various histogram combine/cost-eval functions

// Adds 'in' histogram to 'out'
static void HistogramAdd(const VP8LHistogram* const in,
                         VP8LHistogram* const out) {
  int i;
  for (i = 0; i < PIX_OR_COPY_CODES_MAX; ++i) {
    out->literal_[i] += in->literal_[i];
  }
  for (i = 0; i < NUM_DISTANCE_CODES; ++i) {
    out->distance_[i] += in->distance_[i];
  }
  for (i = 0; i < 256; ++i) {
    out->red_[i] += in->red_[i];
    out->blue_[i] += in->blue_[i];
    out->alpha_[i] += in->alpha_[i];
  }
}

static int GetCombinedHistogramEntropy(const VP8LHistogram* const a,
                                       const VP8LHistogram* const b,
                                       double cost_threshold,
                                       double* cost) {
  const int palette_code_bits =
      (a->palette_code_bits_ > b->palette_code_bits_) ? a->palette_code_bits_ :
                                                        b->palette_code_bits_;
  *cost += GetCombinedEntropy(a->literal_, b->literal_,
                              VP8LHistogramNumCodes(palette_code_bits));
  *cost += ExtraCostCombined(a->literal_ + 256, b->literal_ + 256,
                             NUM_LENGTH_CODES);
  if (*cost > cost_threshold) return 0;

  *cost += GetCombinedEntropy(a->red_, b->red_, 256);
  if (*cost > cost_threshold) return 0;

  *cost += GetCombinedEntropy(a->blue_, b->blue_, 256);
  if (*cost > cost_threshold) return 0;

  *cost += GetCombinedEntropy(a->alpha_, b->alpha_, 256);
  if (*cost > cost_threshold) return 0;

  *cost += GetCombinedEntropy(a->distance_, b->distance_, NUM_DISTANCE_CODES);
  *cost += ExtraCostCombined(a->distance_, b->distance_, NUM_DISTANCE_CODES);
  if (*cost > cost_threshold) return 0;

  return 1;
}

// Performs out = a + b, computing the cost C(a+b) - C(a) - C(b) while comparing
// to the threshold value 'cost_threshold'. The score returned is
//  Score = C(a+b) - C(a) - C(b), where C(a) + C(b) is known and fixed.
// Since the previous score passed is 'cost_threshold', we only need to compare
// the partial cost against 'cost_threshold + C(a) + C(b)' to possibly bail-out
// early.
static double HistogramAddEval(const VP8LHistogram* const a,
                               const VP8LHistogram* const b,
                               VP8LHistogram* const out,
                               double cost_threshold) {
  double cost = 0;
  const double sum_cost = a->bit_cost_ + b->bit_cost_;
  int i;
  cost_threshold += sum_cost;

  if (GetCombinedHistogramEntropy(a, b, cost_threshold, &cost)) {
    for (i = 0; i < PIX_OR_COPY_CODES_MAX; ++i) {
      out->literal_[i] = a->literal_[i] + b->literal_[i];
    }
    for (i = 0; i < NUM_DISTANCE_CODES; ++i) {
      out->distance_[i] = a->distance_[i] + b->distance_[i];
    }
    for (i = 0; i < 256; ++i) {
      out->red_[i] = a->red_[i] + b->red_[i];
      out->blue_[i] = a->blue_[i] + b->blue_[i];
      out->alpha_[i] = a->alpha_[i] + b->alpha_[i];
    }
    out->palette_code_bits_ = (a->palette_code_bits_ > b->palette_code_bits_) ?
                              a->palette_code_bits_ : b->palette_code_bits_;
    out->bit_cost_ = cost;
  }

  return cost - sum_cost;
}

// Same as HistogramAddEval(), except that the resulting histogram
// is not stored. Only the cost C(a+b) - C(a) is evaluated. We omit
// the term C(b) which is constant over all the evaluations.
static double HistogramAddThresh(const VP8LHistogram* const a,
                                 const VP8LHistogram* const b,
                                 double cost_threshold) {
  double cost = -a->bit_cost_;
  GetCombinedHistogramEntropy(a, b, cost_threshold, &cost);
  return cost;
}

// -----------------------------------------------------------------------------

// The structure to keep track of cost range for the three dominant entropy
// symbols.
// TODO(skal): Evaluate if float can be used here instead of double for
// representing the entropy costs.
typedef struct {
  double literal_max_;
  double literal_min_;
  double red_max_;
  double red_min_;
  double blue_max_;
  double blue_min_;
} DominantCostRange;

static void DominantCostRangeInit(DominantCostRange* const c) {
  c->literal_max_ = 0.;
  c->literal_min_ = MAX_COST;
  c->red_max_ = 0.;
  c->red_min_ = MAX_COST;
  c->blue_max_ = 0.;
  c->blue_min_ = MAX_COST;
}

static void UpdateDominantCostRange(
    const VP8LHistogram* const h, DominantCostRange* const c) {
  if (c->literal_max_ < h->literal_cost_) c->literal_max_ = h->literal_cost_;
  if (c->literal_min_ > h->literal_cost_) c->literal_min_ = h->literal_cost_;
  if (c->red_max_ < h->red_cost_) c->red_max_ = h->red_cost_;
  if (c->red_min_ > h->red_cost_) c->red_min_ = h->red_cost_;
  if (c->blue_max_ < h->blue_cost_) c->blue_max_ = h->blue_cost_;
  if (c->blue_min_ > h->blue_cost_) c->blue_min_ = h->blue_cost_;
}

static void UpdateHistogramCost(VP8LHistogram* const h) {
  const double alpha_cost = PopulationCost(h->alpha_, 256);
  const double distance_cost =
      PopulationCost(h->distance_, NUM_DISTANCE_CODES) +
      ExtraCost(h->distance_, NUM_DISTANCE_CODES);
  const int num_codes = VP8LHistogramNumCodes(h->palette_code_bits_);
  h->literal_cost_ = PopulationCost(h->literal_, num_codes) +
                     ExtraCost(h->literal_ + 256, NUM_LENGTH_CODES);
  h->red_cost_ = PopulationCost(h->red_, 256);
  h->blue_cost_ = PopulationCost(h->blue_, 256);
  h->bit_cost_ = h->literal_cost_ + h->red_cost_ + h->blue_cost_ +
                 alpha_cost + distance_cost;
}

static int GetBinIdForEntropy(double min, double max, double val) {
  const double range = max - min + 1e-6;
  const double delta = val - min;
  return (int)(NUM_PARTITIONS * delta / range);
}

// TODO(vikasa): Evaluate, if there's any correlation between red & blue.
static int GetHistoBinIndex(
    const VP8LHistogram* const h, const DominantCostRange* const c) {
  const int bin_id =
      GetBinIdForEntropy(c->blue_min_, c->blue_max_, h->blue_cost_) +
      NUM_PARTITIONS * GetBinIdForEntropy(c->red_min_, c->red_max_,
                                          h->red_cost_) +
      NUM_PARTITIONS * NUM_PARTITIONS * GetBinIdForEntropy(c->literal_min_,
                                                           c->literal_max_,
                                                           h->literal_cost_);
  assert(bin_id < BIN_SIZE);
  return bin_id;
}

// Construct the histograms from backward references.
static void HistogramBuild(
    int xsize, int histo_bits, const VP8LBackwardRefs* const backward_refs,
    VP8LHistogramSet* const init_histo) {
  int i;
  int x = 0, y = 0;
  const int histo_xsize = VP8LSubSampleSize(xsize, histo_bits);
  VP8LHistogram** const histograms = init_histo->histograms;
  assert(histo_bits > 0);
  // Construct the Histo from a given backward references.
  for (i = 0; i < backward_refs->size; ++i) {
    const PixOrCopy* const v = &backward_refs->refs[i];
    const int ix = (y >> histo_bits) * histo_xsize + (x >> histo_bits);
    VP8LHistogramAddSinglePixOrCopy(histograms[ix], v);
    x += PixOrCopyLength(v);
    while (x >= xsize) {
      x -= xsize;
      ++y;
    }
  }
}

// Compute the histogram aggregate bit_cost.
static void HistogramAnalyze(
    VP8LHistogramSet* const init_histo, VP8LHistogramSet* const histo_image) {
  int i;
  const int histo_size = init_histo->size;
  VP8LHistogram** const histograms = init_histo->histograms;
  for (i = 0; i < histo_size; ++i) {
    VP8LHistogram* const histo = histograms[i];
    histo->bit_cost_ = VP8LHistogramEstimateBits(histo);
    // Copy histograms from init_histo[] to histo_image[].
    *histo_image->histograms[i] = *histo;
  }
}

// Partition Histograms to different entropy bins for three dominant (literal,
// red and blue) symbol costs and compute the histogram aggregate bit_cost.
static void HistogramAnalyzeBin(
    VP8LHistogramSet* const init_histo, VP8LHistogramSet* const histo_image,
    int16_t* const bin_map) {
  int i;
  const int histo_size = init_histo->size;
  VP8LHistogram** const histograms = init_histo->histograms;
  const int bin_depth = init_histo->size + 1;
  DominantCostRange cost_range;
  DominantCostRangeInit(&cost_range);

  // Analyze the dominant (literal, red and blue) entropy costs.
  for (i = 0; i < histo_size; ++i) {
    VP8LHistogram* const histo = histograms[i];
    UpdateHistogramCost(histo);
    // Copy histograms from init_histo[] to histo_image[].
    *histo_image->histograms[i] = *histo;
    UpdateDominantCostRange(histo, &cost_range);
  }

  // bin-hash histograms on three of the dominant (literal, red and blue)
  // symbol costs.
  for (i = 0; i < histo_size; ++i) {
    int num_histos;
    VP8LHistogram* const histo = histograms[i];
    const int16_t bin_id = (int16_t)GetHistoBinIndex(histo, &cost_range);
    const int bin_offset = bin_id * bin_depth;
    // bin_map[n][0] for every bin 'n' maintains the counter for the number of
    // histograms in that bin.
    // Get and increment the num_histos in that bin.
    num_histos = ++bin_map[bin_offset];
    assert(bin_offset + num_histos < bin_depth * BIN_SIZE);
    // Add histogram i'th index at num_histos (last) position in the bin_map.
    bin_map[bin_offset + num_histos] = i;
  }
}

// Compact the histogram set by moving the valid one left in the set to the
// head and moving the ones that have been merged to other histograms towards
// the end.
// TODO(vikasa): Evaluate if this method can be avoided by altering the code
// logic of HistogramCombineBin main loop.
static void HistogramCompactBins(VP8LHistogramSet* const histo_image) {
  int start = 0;
  int end = histo_image->size - 1;
  while (start < end) {
    while (start <= end &&
           histo_image->histograms[start] != NULL &&
           histo_image->histograms[start]->bit_cost_ != 0.) {
      ++start;
    }
    while (start <= end &&
           histo_image->histograms[end]->bit_cost_ == 0.) {
      histo_image->histograms[end] = NULL;
      --end;
    }
    if (start < end) {
      assert(histo_image->histograms[start] != NULL);
      assert(histo_image->histograms[end] != NULL);
      *histo_image->histograms[start] = *histo_image->histograms[end];
      histo_image->histograms[end] = NULL;
      --end;
    }
  }
  histo_image->size = end + 1;
}

static void HistogramCombineBin(VP8LHistogramSet* const histo_image,
                                VP8LHistogram* const histos,
                                int bin_depth,
                                int16_t* const bin_map) {
  int bin_id;
  VP8LHistogram* cur_combo = histos;

  for (bin_id = 0; bin_id < BIN_SIZE; ++bin_id) {
    const int bin_offset = bin_id * bin_depth;
    const int num_histos = bin_map[bin_offset];
    const int idx1 = bin_map[bin_offset + 1];
    int n;
    for (n = 2; n <= num_histos; ++n) {
      const int idx2 = bin_map[bin_offset + n];
      const double bit_cost_idx2 = histo_image->histograms[idx2]->bit_cost_;
      if (bit_cost_idx2 > 0.) {
        const double bit_cost_thresh = -bit_cost_idx2 * 0.1;
        const double curr_cost_diff =
            HistogramAddEval(histo_image->histograms[idx1],
                             histo_image->histograms[idx2],
                             cur_combo, bit_cost_thresh);
        if (curr_cost_diff < bit_cost_thresh) {
          *histo_image->histograms[idx1] = *cur_combo;
          histo_image->histograms[idx2]->bit_cost_ = 0.;
        }
      }
    }
  }
  HistogramCompactBins(histo_image);
}

static uint32_t MyRand(uint32_t *seed) {
  *seed *= 16807U;
  if (*seed == 0) {
    *seed = 1;
  }
  return *seed;
}

static void HistogramCombine(VP8LHistogramSet* const histo_image,
                             VP8LHistogram* const histos, int quality) {
  int iter;
  uint32_t seed = 0;
  int tries_with_no_success = 0;
  int histo_image_size = histo_image->size;
  const int iter_mult = (quality < 25) ? 2 : 2 + (quality - 25) / 8;
  const int outer_iters = histo_image_size * iter_mult;
  const int num_pairs = histo_image_size / 2;
  const int num_tries_no_success = outer_iters / 2;
  const int min_cluster_size = 2;
  VP8LHistogram* cur_combo = histos + 0;    // trial merged histogram
  VP8LHistogram* best_combo = histos + 1;   // best merged histogram so far

  // Collapse similar histograms in 'histo_image'.
  for (iter = 0;
       iter < outer_iters && histo_image_size >= min_cluster_size;
       ++iter) {
    double best_cost_diff = 0.;
    int best_idx1 = -1, best_idx2 = 1;
    int j;
    const int num_tries =
        (num_pairs < histo_image_size) ? num_pairs : histo_image_size;
    seed += iter;
    for (j = 0; j < num_tries; ++j) {
      double curr_cost_diff;
      // Choose two histograms at random and try to combine them.
      const uint32_t idx1 = MyRand(&seed) % histo_image_size;
      const uint32_t tmp = (j & 7) + 1;
      const uint32_t diff =
          (tmp < 3) ? tmp : MyRand(&seed) % (histo_image_size - 1);
      const uint32_t idx2 = (idx1 + diff + 1) % histo_image_size;
      if (idx1 == idx2) {
        continue;
      }

      // Calculate cost reduction on combining.
      curr_cost_diff = HistogramAddEval(histo_image->histograms[idx1],
                                        histo_image->histograms[idx2],
                                        cur_combo, best_cost_diff);
      if (curr_cost_diff < best_cost_diff) {    // found a better pair?
        {     // swap cur/best combo histograms
          VP8LHistogram* const tmp_histo = cur_combo;
          cur_combo = best_combo;
          best_combo = tmp_histo;
        }
        best_cost_diff = curr_cost_diff;
        best_idx1 = idx1;
        best_idx2 = idx2;
      }
    }

    if (best_idx1 >= 0) {
      *histo_image->histograms[best_idx1] = *best_combo;
      // swap best_idx2 slot with last one (which is now unused)
      --histo_image_size;
      if (best_idx2 != histo_image_size) {
        histo_image->histograms[best_idx2] =
            histo_image->histograms[histo_image_size];
        histo_image->histograms[histo_image_size] = NULL;
      }
      tries_with_no_success = 0;
    }
    if (++tries_with_no_success >= num_tries_no_success) {
      break;
    }
  }
  histo_image->size = histo_image_size;
}

// -----------------------------------------------------------------------------
// Histogram refinement

// Find the best 'out' histogram for each of the 'in' histograms.
// Note: we assume that out[]->bit_cost_ is already up-to-date.
static void HistogramRemap(const VP8LHistogramSet* const init_histo,
                           const VP8LHistogramSet* const histo_image,
                           uint16_t* const symbols) {
  int i;
  for (i = 0; i < init_histo->size; ++i) {
    int best_out = 0;
    double best_bits = HistogramAddThresh(histo_image->histograms[0],
                                          init_histo->histograms[i], MAX_COST);
    int k;
    for (k = 1; k < histo_image->size; ++k) {
      const double cur_bits = HistogramAddThresh(histo_image->histograms[k],
                                                 init_histo->histograms[i],
                                                 best_bits);
      if (cur_bits < best_bits) {
        best_bits = cur_bits;
        best_out = k;
      }
    }
    symbols[i] = best_out;
  }

  // Recompute each out based on raw and symbols.
  for (i = 0; i < histo_image->size; ++i) {
    HistogramClear(histo_image->histograms[i]);
  }

  for (i = 0; i < init_histo->size; ++i) {
    HistogramAdd(init_histo->histograms[i],
                 histo_image->histograms[symbols[i]]);
  }
}

int VP8LGetHistoImageSymbols(int xsize, int ysize,
                             const VP8LBackwardRefs* const refs,
                             int quality, int histo_bits, int cache_bits,
                             VP8LHistogramSet* const histo_image,
                             uint16_t* const histogram_symbols) {
  int ok = 0;
  const int histo_xsize = histo_bits ? VP8LSubSampleSize(xsize, histo_bits) : 1;
  const int histo_ysize = histo_bits ? VP8LSubSampleSize(ysize, histo_bits) : 1;
  const int histo_image_raw_size = histo_xsize * histo_ysize;

  // The bin_map for every bin follows following semantics:
  // bin_map[n][0] = num_histo; // The number of histograms in that bin.
  // bin_map[n][1] = index of first histogram in that bin;
  // bin_map[n][num_histo] = index of last histogram in that bin;
  // bin_map[n][num_histo + 1] ... bin_map[n][bin_depth - 1] = un-used indices.
  const int bin_depth = histo_image_raw_size + 1;
  int16_t* bin_map = NULL;
  VP8LHistogram* const histos = (VP8LHistogram*)malloc(2 * sizeof(*histos));
  VP8LHistogramSet* const init_histo =
      VP8LAllocateHistogramSet(histo_image_raw_size, cache_bits);

  if (init_histo == NULL || histos == NULL) {
    goto Error;
  }

  // Don't attempt linear bin-partition heuristic for:
  // Histograms of small sizes, as bin_map will be very sparse and;
  // Higher qualities (> 90), to preserve the compression gains at those
  // quality settings.
  if (init_histo->size > 2 * BIN_SIZE && quality < 90) {
    const int bin_map_size = (uint64_t)bin_depth * BIN_SIZE;
    bin_map = (int16_t*)WebPSafeCalloc(bin_map_size, sizeof(*bin_map));
    if (bin_map == NULL) goto Error;
  }

  // Construct the histogram from backward references.
  HistogramBuild(xsize, histo_bits, refs, init_histo);

  if (bin_map != NULL) {
    HistogramAnalyzeBin(init_histo, histo_image, bin_map);
    HistogramCombineBin(histo_image, histos, bin_depth, bin_map);
  } else {
    HistogramAnalyze(init_histo, histo_image);
  }

  // Collapse similar histograms.
  HistogramCombine(histo_image, histos, quality);

  // Find the optimal map from original histograms to the final ones.
  HistogramRemap(init_histo, histo_image, histogram_symbols);

  ok = 1;

 Error:
  free(bin_map);
  free(init_histo);
  free(histos);
  return ok;
}
