/**
 * @file IrenderPass.h
 * @brief Public interface for RenderPass type
 **/
#pragma once

/**
 * RenderPass��public�C���^�[�t�F�C�X
 */
class IRenderPass {
public:
  /**
   * @return MuM��
   */
  virtual char *getMuM() = 0;

  /**
   * @return �p�XID
   */
  virtual char *getId() = 0;

  /**
   * @return Uniform�̐�
   */
  virtual int getUniformsNum() = 0;

  /**
   * @param uniformIndex Uniform�̃C���f�b�N�X
   * @return Uniform��
   */
  virtual char *getUniformName( int uniformIndex ) = 0;

  /**
   * @param uniformIndex Uniform�̃C���f�b�N�X
   * @return Uniform�̒l�z��̃T�C�Y
   */
  virtual int getUniformSize( int uniformIndex ) = 0;

  /**
   * @param uniformIndex Uniform�̃C���f�b�N�X
   * @return Uniform�̒l�z��
   */
  virtual float *getUniformValues( int uniformIndex ) = 0;
};
