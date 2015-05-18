/**
 * @file matrix.h
 * @brief 4x4 Matrix type
 **/
#pragma once


/**
 * @brief 4x4行列クラス
 * OpenGLに使用される4行4列のクラスです。
 * 行優先(モデル視点)のDirectXとは異なり、列優先(空間視点)のため、
 * 配列の配置図は次のようになります。
 * ┃a0 a4  a8 a12┃ 
 * ┃a1 a5  a9 a13┃
 * ┃a2 a6 a10 a14┃
 * ┃a3 a7 a11 a15┃
 *
 */
class Matrix
{
 public:
  
  /**
   * Identityをロードする
   **/
  void setIdentity();

  /**
   * ゼロクリアする
   **/
  void setZero();

  /**
   * @brief 行列を指定の移動変換に設定される。 以前の変換が失われます。
   * 移動変換を現在の変換行列に掛け合わせるには、translate()メソッドをご使用ください。
   **/
  void setTranslation( float tx, float ty, float tz );

  /**
   * @brief 行列をスケール変換に設定される。 以前の変換が失われる。
   * スケール変換を現在の変換行列に掛け合わせるには、scale()メソッドをご使用ください。
   **/
  void setScale( float sx, float sy, float sz );

  /**
   * @brief X回転をスケール変換に設定される。 以前の変換が失われる。
   * X回転変換を現在の変換行列に掛け合わせるには、rotateX()メソッドをご使用ください。
   **/
  void setRotationX( float angle );

  /**
   * @brief Y回転をスケール変換に設定される。 以前の変換が失われる。
   * Y回転変換を現在の変換行列に掛け合わせるには、rotateY()メソッドをご使用ください。
   **/
  void setRotationY( float angle );

  /**
   * @brief Z回転をスケール変換に設定される。 以前の変換が失われる。
   * Z回転変換を現在の変換行列に掛け合わせるには、rotateZ()メソッドをご使用ください。
   **/
  void setRotationZ( float angle );

  /**
   * @brief glFrustumfと同じ効果
   */
  void setFrustum( float l, float r, float b, float t, float n, float f );
  
  /**
   * @brief glOrthofと同じ効果
   */
  void setOrtho( float l, float r, float b, float t, float n, float f );

  /**
   * @brief gluPerspectiveと同じ効果
   */
  void setPerspective( float fovy, float aspectRatio, float near, float far );

  /**
   * @brief この変換行列を指定の方向へ移動させます
   **/
  void translate( float tx, float ty, float tz );

  /**
   * @brief この変換行列を指定の係数でスケールさせる
   **/
  void scale( float sx, float sy, float sz );

  /**
   * @brief この変換行列を指定の角度でX軸を中心に回転させます
   **/
  void rotateX( float angle );

  /**
   * @brief この変換行列を指定の角度でY軸を中心に回転させます
   **/
  void rotateY( float angle );

  /**
   * @brief この変換行列を指定の角度でZ軸を中心に回転させます
   **/
  void rotateZ( float angle );

  /**
   * @brief v3eyeを視点にして、カメラがv3centerを向くように変換行列を設定する
   **/
  void lookAt( float* v3eye, float* v3center, float* v3up );
  
  /**
   * @brief 指定の3Dベクトルにこの行列変換をかけあわせます
   **/
  void transform3( float* vec3 );

  /**
   * @brief 指定の4Dベクトルにこの行列変換をかけあわせます
   **/
  void transform4( float* vec4 );

  /**
   * @brief 逆行列に変換する
   **/
  void invert();

  /**
   * @brief 指定の行列を掛け合わせて、結果を現行列に保存します
   * operandが変更されません、この行列が変更されます。
   * 結果 = this * operand
   *
   * @param operand 行列オペランド
   **/
  void multiply(Matrix* operand);

  /**
   * @brief 指定の行列を掛け合わせて、結果を現行列に保存します
   * operandが変更されません、この行列が変更されます。
   * 結果 = operand * this
   *
   **/
  void premultiply(Matrix* operand);

  /**
   * @brief 指定の行列から情報をコピーする
   *
   * @param src データがコピーされる元
   **/
  void copyFrom(Matrix const* src);

  /**
   * @brief 3x4行列として出力します
   * @param out 出力バッファー(最低12 floatのサイズがなければいけない)
   **/
  void to3x4(float* out);

  /**
   * @return float*ポインター
   **/
  float* getMatrixPointer();

  /**
   * Log the matrix
   **/
  void toString(char* outBuffer);

 private:
  float data[16];
};
