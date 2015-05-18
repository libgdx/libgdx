/**
 * @file IrenderPass.h
 * @brief Public interface for RenderPass type
 **/
#pragma once

/**
 * RenderPassのpublicインターフェイス
 */
class IRenderPass {
public:
  /**
   * @return MuM名
   */
  virtual char *getMuM() = 0;

  /**
   * @return パスID
   */
  virtual char *getId() = 0;

  /**
   * @return Uniformの数
   */
  virtual int getUniformsNum() = 0;

  /**
   * @param uniformIndex Uniformのインデックス
   * @return Uniform名
   */
  virtual char *getUniformName( int uniformIndex ) = 0;

  /**
   * @param uniformIndex Uniformのインデックス
   * @return Uniformの値配列のサイズ
   */
  virtual int getUniformSize( int uniformIndex ) = 0;

  /**
   * @param uniformIndex Uniformのインデックス
   * @return Uniformの値配列
   */
  virtual float *getUniformValues( int uniformIndex ) = 0;
};
