/**
 * @file ImergeGroup.h
 * @brief Public interface for merge group type
 **/
#pragma once

#include "Iobject.h"
#include "IpolygonMap.h"
#include "matrix.h"
#include "types.h"

/**
 * MergeGroupのpublicインターフェース
 **/
class IMergeGroup : public IObject
{
 public:

  /**
   * @brief ここまでの追加を排除し、結合グループをリセットします。
   **/
  virtual void startGroup() = 0;

  /**
   * @brief 結合グループを描画のためにコミットします。　
   * これ以降は追加コマンドが出来ません。
   **/
  virtual void endGroup() = 0;

  /**
   * @brief グループに任意のポリゴンマップを結合する。
   *
   * @param polygonMap ポリゴンマップ
   * @param targetPmId このObjectのどのポリゴンマップに結合するか
   * @param x 結合座標
   * @param y 結合座標
   * @param z 結合座標
   **/
  virtual void addPolygonMap(IPolygonMap* polygonMap, int targetPmId, 
			     float x, float y, float z) = 0;

  /**
   * @brief グループに任意のポリゴンマップを結合する。
   *
   * @param polygonMap ポリゴンマップ
   * @param targetPmId このObjectのどのポリゴンマップに結合するか
   * @param transform 任意変換
   **/
  virtual void addPolygonMap(IPolygonMap* polygonMap, int targetPmId, 
			     Matrix* transform) =0;

  /**
   * @brief グループに任意のオブジェクトを結合する。
   *
   * @param object オブジェクト
   * @param x 結合座標
   * @param y 結合座標
   * @param z 結合座標
   *
   **/
  virtual void addObject(IObject* object, int targetPmId, 
			 float x, float y, float z) = 0;

  /**
   * @brief グループに任意のオブジェクトを結合する。
   *
   * @param object オブジェクト
   * @param targetPmId このObjectのどのポリゴンマップに結合するか
   * @param transform 任意変換
   **/
  virtual void addObject(IObject* object, int targetPmId, 
			 Matrix* transform) =0;

  /**
   * @brief グループに任意の三角配列を結合する。
   * 必ずインデックス0のポリゴンマップに追加されます。
   *
   * @param vertices 頂点数
   * @param triangles 三角数
   * @param points 頂点配列
   * @param indices 三角インデックス配列
   * @param uvs UV配列
   **/
  virtual void addTriangles(int vertices, int triangles, const float* points, 
		    const unsigned short* indices, const float* uvs) = 0;

  /**
   * @brief スプライトをグループに結合します。　結合される長方形は
   * 指定テクスチャーどおりのサイズとなり、頂点を指定の分ずらされます。
   * レンダーコールに同じテクスチャーを指定する流れになります。
   *
   * @param x 結合座標
   * @param y 結合座標
   * @param texture テクスチャー
   *
   **/
  virtual void addSprite(float x, float y, float z, int texture) = 0;

  /**
   * @brief スプライトをグループに結合します。　結合される長方形は
   * 指定どおりのサイズとなり、頂点を指定の分ずらされます。
   * レンダーコールに同じテクスチャーを指定する流れになります。
   *
   * @param x 結合座標
   * @param y 結合座標
   * @param w 長方形の幅
   * @param h 長方形の高さ
   * @param texture テクスチャー
   * @param sx 採用されるテクスチャー部分の長方形の位置
   * @param sy 採用されるテクスチャー部分の長方形の位置
   * @param sw 採用されるテクスチャー部分の長方形の幅
   * @param sh 採用されるテクスチャー部分の長方形の高さ
   **/
  virtual void addSprite(float x, float y, float z, float w, float h, int texture,
			 float sx, float sy, float sw, float sh, bool flipV=false) = 0;

  /**
   * @brief (-0.5, -0.5, 0), (0.5, 0.5, 0)の四角に任意の変換行列を掛け、
   * その結果として変換された頂点でスプライトを追加します。
   *
   * @param texture テクスチャー
   * @param sx 採用されるテクスチャー部分の長方形の位置
   * @param sy 採用されるテクスチャー部分の長方形の位置
   * @param sw 採用されるテクスチャー部分の長方形の幅
   * @param sh 採用されるテクスチャー部分の長方形の高さ
   * @param transform 行列変換
   **/
  virtual void addSprite(int texture, float sx, float sy, 
			 float sw, float sh, Matrix* transform, bool flipV=false) = 0;

 /**
   * @brief スプライトをグループに結合します。　結合される長方形は
   * 指定どおりのサイズとなり、頂点を指定の分ずらされます。
   * レンダーコールに同じテクスチャーを指定する流れになります。
   *
   * @param x 結合座標
   * @param y 結合座標
   * @param w 長方形の幅
   * @param h 長方形の高さ
   * @param u 採用されるテクスチャー部分のu値
   * @param v 採用されるテクスチャー部分のv値
   * @param u2 採用されるテクスチャー部分のu2値
   * @param v2 採用されるテクスチャー部分のv2値
   **/
  virtual void addSpriteUV(float x, float y, float z, float w, float h, 
			   float u, float v, float u2, float v2) = 0;

 /**
   * @brief 四角形をグループに結合します。
   * 長方形である必要はありません。
   * 頂点は反時計回りに1～4を指定してください。
   *
   * @param vertex1 頂点1（座標XY及びUV値）
   * @param vertex2 頂点2（座標XY及びUV値）
   * @param vertex3 頂点3（座標XY及びUV値）
   * @param vertex4 頂点4（座標XY及びUV値）
   * @param z 全頂点の座標Z
   **/
  virtual void addQuad(SimpleVertex *vertex1, SimpleVertex *vertex2, SimpleVertex *vertex3, SimpleVertex *vertex4, float z) = 0;
};
