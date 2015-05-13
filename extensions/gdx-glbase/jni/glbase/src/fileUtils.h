/**
 * @file fileUtils.h
 * @brief Set of utilities to read values from memory buffers
 **/
#pragma once 

#include "types.h"
#include <stdio.h>

/**
 * Class wrapper for a set of static methods
 **/
class FileUtils
{
public:

  /**
   * byte値を読み込む
   * 
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * 
   * @return  読み込まれたbyte値
   */
  static byte readByte( const byte *buffer, int &offset );

  /**
   * char値を読み込む
   * 
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * 
   * @return  読み込まれたchar値
   */
  static char readChar( const byte *buffer, int &offset );

  /**
   * int値を読み込む
   * 
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * 
   * @return  読み込まれたint値
   */
  static int readInt( const byte *buffer, int &offset );

  /**
   * unsigned short値を読み込む
   * 
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * 
   * @return  読み込まれたunsigned short値
   */
  static unsigned short readUshort( const byte *buffer, int &offset );

  /**
   * byte配列を読み込む
   *
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * @param length読み込むbyteの数
   * @return 読み込まれたデータ
   */
  static byte* readBytes( const byte *buffer, int &offset, int length );

  /**
   * 文字列を読み込む
   *
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * @param length読み込む文字列の長さ
   * @return 読み込まれたデータ
   */
  static char* readString( const byte *buffer, int &offset, int length );
  
  /**
   * ushort配列を読み込む
   *
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * @param length読み込むushortの数
   * @return 読み込まれたデータ
   */
  static unsigned short* readUshorts( const byte *buffer, int &offset, int length );

  /**
   * int配列を読み込む
   *
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * @param   length読み込むintの数
   * @return 読み込まれたデータ
   */
  static int* readInts( const byte *buffer, int &offset, int length );

  /**
   * float配列を読み込む
   *
   * @param   buffer  バッファ
   * @param   offset  バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * @param   length 読み込むfloatの数
   * @return 読み込まれたデータ
   */
  static float* readFloats( const byte *buffer, int &offset, int length );

  /**
   * float配列を読み込む(メモリー確保なし版)
   *
   * @param buffer バッファ
   * @param offset バッファの読み込み開始位置（読み込み後は終了位置を返す）
   * @param length 読み込むfloatの数
   * @param outData このにデータがコピーされる
   **/
  static void readFloats( const byte *buffer, int &offset, int length, float* outData );
};
