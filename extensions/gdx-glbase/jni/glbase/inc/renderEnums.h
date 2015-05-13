/*!
 * @file renderEnums.h
 * 
 * @brief       描画設定用列挙体.
 * 
 */

#ifndef __RENDER_ENUMS_H__
#define __RENDER_ENUMS_H__

class RenderEnums {

 public:

  /*!
   * @brief   ブレンドモード列挙体.
   */
  enum BlendMode {

    BLENDMODE_DEFAULT = 0,//!< デフォルト.
    BLENDMODE_NONE,//!< 置き換え.
    BLENDMODE_DONOTHING,//!< 何もしない.
    BLENDMODE_ALPHA_BLEND,//!< アルファブレンド.
    BLENDMODE_ADD,//!< 加算.
    BLENDMODE_ADDNALPHA,//!< 加算でアルファブレンド.
    BLENDMODE_MULTIPLY,//!< 乗算.
    BLENDMODE_NEGATION,//!< 反転.
    BLENDMODE_SCREEN,//!< スクリーン.
    BLENDMODE_SUBTRACT,//!< 減算.
    BLENDMODE_SUBTRACTNALPHA,//!< 減算でアルファブレンド.
    BLENDMODE_MAX,
  };

  /*!
   * @brief   ブレンド関数列挙体.
   */
  enum BlendFunc {

    BLENDFUNC_DONOTHING = 0,//!< 何もしない.
    BLENDFUNC_ALPHA_BLEND,//!< アルファブレンド.
    BLENDFUNC_ADD,//!< 加算.
    BLENDFUNC_ADDNALPHA,//!< 加算でアルファブレンド.
    BLENDFUNC_MULTIPLY,//!< 乗算.
    BLENDFUNC_NEGATION,//!< 反転.
    BLENDFUNC_SCREEN,//!< スクリーン.
    BLENDFUNC_SUBTRACT,//!< 減算.
    BLENDFUNC_SUBTRACTNALPHA,//!< 減算でアルファブレンド.
    BLENDFUNC_MAX,
  };

  /*!
   * @brief   ブレンド式列挙体.
   */
  enum BlendEq {
    BLENDEQ_ADD = 0,
    BLENDEQ_SUBTRACT,
    BLENDEQ_REV_SUBTRACT,
  };
  
  /*!
   * @brief   クリアモード列挙体.
   */
  enum ClearMode {

    CLEARMODE_COLOR_AND_DEPTH = 0,//!< カラーと奥行き.
    CLEARMODE_COLOR_ONLY,//!< カラーのみ.
    CLEARMODE_DEPTH_ONLY,//!< 奥行きのみ.
    CLEARMODE_NONE,//!< しない.
    CLEARMODE_MAX,
  };
  
  /*!
   * @brief   デプス比較関数列挙体.
   */
  enum DepthFunc {

    DEPTHFUNC_NEVER = 0,//!< 必ず失敗.
    DEPTHFUNC_LESS,//!< 小さい場合.
    DEPTHFUNC_EQUAL,//!< 同じ場合.
    DEPTHFUNC_LEQUAL,//!< 以下の場合.
    DEPTHFUNC_GREATER,//!< 大きい場合.
    DEPTHFUNC_NOTEQUAL,//!< 同じじゃない場合.
    DEPTHFUNC_GEQUAL,//!< 以上の場合.
    DEPTHFUNC_ALWAYS,//!< 必ず成功.
    DEPTHFUNC_MAX,
  };
  
  /*!
   * @brief   モデルの種類列挙体.
   */
  enum ModelType {

    MODELTYPE_DEFAULT = 0,//!< デフォルト.
    MODELTYPE_TEXTURE_FULL,//!< テクスチャ全体表示.
    MODELTYPE_BBOX,//!< BBOX表示.
    MODELTYPE_PARTICLE,//!< パーティクル表示.
    MODELTYPE_MAX,
  };
  
  /*!
   * @brief   カリングモード列挙体.
   */
  enum CullingMode {

    CULLING_DEFAULT = 0,//!< デフォルト.
    CULLING_OFF,//!< カリングを無効.
    CULLING_BACK,//!< 裏面カリング.
    CULLING_FRONT,//!< 表面カリング.
    CULLING_FRONT_AND_BACK,//!< 両面カリング.
    CULLING_MAX,
  };

  /*!
   * @brief   テクスチャの種類列挙体.
   */
  enum TextureType {
    TEXTYPE_DEFAULT = 0,//!< デフォルト.
    TEXTYPE_FBO_1,//!< FBO1.
    TEXTYPE_FBO_2,//!< FBO2.
    TEXTYPE_FBO_3,//!< FBO3.
    TEXTYPE_FBO_4,//!< FBO4.
    TEXTYPE_FBO_5,//!< FBO5.
    TEXTYPE_FBO_6,//!< FBO6.
    TEXTYPE_FBO_7,//!< FBO7.
    TEXTYPE_FBO_8,//!< FBO8.
    TEXTYPE_FBO_9,//!< FBO9.
    TEXTYPE_FBO_10,//!< FBO10.
    TEXTYPE_FBO_11,//!< FBO11.
    TEXTYPE_FBO_12,//!< FBO12.
    TEXTYPE_FBO_13,//!< FBO13.
    TEXTYPE_FBO_14,//!< FBO14.
    TEXTYPE_FBO_15,//!< FBO15.
	TEXTYPE_PP_DIV8_WDEPTH_FBO = 101,	//ポストプロセスFBO（サイズ÷8、デプスあり）
	TEXTYPE_PP_DIV8_NODEPTH_FBO,		//ポストプロセスFBO（サイズ÷8、デプスなし）
	TEXTYPE_PP_DIV2_WDEPTH_FBO,			//ポストプロセスFBO（サイズ÷2、デプスあり）
	TEXTYPE_PP_DIV2_NODEPTH_FBO,		//ポストプロセスFBO（サイズ÷2、デプスなし）
  };

  /*!
   * @brief   Uniformの設定.
   */
  enum UniformPriority {

    UNIPRIORITY_MRF_FIRST = 0,//!< 設定を行う.
    UNIPRIORITY_MRF_NONE,//!< 設定を行わない.
    UNIPRIORITY_MAX,
  };
};


#endif //__RENDER_ENUMS_H__

