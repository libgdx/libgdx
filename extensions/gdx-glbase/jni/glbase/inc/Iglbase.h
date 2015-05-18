/**
 * @file Iglbase.h
 * @brief Public interface for GLBase: wrap library over GL ES 2.0
 **/
#pragma once

#include <stdarg.h>
#include <stdio.h>

#include "types.h"
#include "renderEnums.h"

// Forwards
class IObject;
class IMrf;
class IMergeGroup;
class IPolygonMap;
class IAnimationPlayer;
class IRenderQueue;
class Matrix;
class DrawCall;
class MyUniformValue;

class IGLBase
{
  
public:

  /**
   * @brief 標準のシェーダーープログラムのシェーダーID
   * registerDrawCallのパラメターとして使用
   **/
  static const int DEFAULT_SHADER = 0;


  /**
   * IGLBaseインターフェースのシングルトン実体化を取得する
   **/ 
  static IGLBase* get();

  /**
   * @brief ログ関数を設定する
   *
   * @param f ログ関数
   **/
  virtual void setTraceFunction(LOGFUNC f) = 0;

  /**
   * @brief エラーログ関数を設定する
   *
   * @param fエラーログ関数
   **/
  virtual void setEtraceFunction(LOGFUNC f) = 0;

  /**
   * @brief 
   *
   * @param fmt
   **/
  virtual void dotrace(char const* fmt, ...) = 0;

  /**
   * @brief 
   *
   * @param fmt
   **/
  virtual void doetrace(char const* fmt, ...) = 0;

  /**
   * @brief GLを初期化し、標準の機能を準備します
   * GLBaseを使う際に、必ずこのメソッドを一度だけ
   * 呼び出すべきです
   **/
  virtual void initialize(int screenWidth, int screenHeight) = 0;

  /**
   * @brief bo3バイトデータからObject実態をロードします
   * このオブジェクト以下の全てのポリゴンマップのプライオリティーが"default"となります。
   *
   * @param data bo3データ
   * @param length bo3データのバイト長さ
   *
   * @return 成功の場合はロードされたObject、失敗の場合はNULL
   * 失敗の場合、データが正常なbo3になっていない可能性があります
   **/
  virtual IObject* loadBo3(byte* data, int length, bool gpuOnly=false) = 0;

  /**
   * @brief bo3モデルから一部の頂点・インデックスデータを利用し、
   * サブオブジェクトを抽出します。
   *
   * @param object 元のモデル
   * @param vertexOffset 頂点オフセット
   * @param triangleOffset インデックスのオフセット
   * @param vertexLength 抽出する頂点数
   * @param triangleLength 抽出するインデックス数
   * @param layer レイヤー番号
   * @param polygonMap ポリゴンマップ番号
   *
   * @return 抽出されたオブジェクト。 レンダー初期化されていませんので、
   * 使用する前に一度initRenderEnv()を使う必要があります。
   **/
  virtual IObject* subObject(IObject* object, int vertexOffset, int triangleOffset,
			     int vertexLength, int triangleLength, int layer, int polygonMap) = 0;

  /**
   * @brief 多数のポリゴンマップデータをリアルタイムで結合出来る
   * マージグループを作成します。　このグループを使うには、
   * 描くフレームnewGroup()を一度呼び出した後、マージさせる
   * すべてのポリゴンマップをaddPolygonMap()で追加し、
   * このIMergeGroupをDrawCallに渡します。
   *
   * 結合させるポリゴンマップは全部指定されたベース
   * オブジェクトと同じ設定になります。
   *
   * このグループの結合結果として、1レイヤーと1ポリゴンマップのオブジェクトになります。
   * 
   * @param base このオブジェクトの空コピーが、このIMergeGroupの元になります。頂点情報が全部無視されます。
   * @param maxVertices 総合頂点数の最大値。これを超えた数の頂点を追加しても自動伸長されます。
   * @param polygonMaps 生成するポリゴンマップ数
   **/
  virtual IMergeGroup* createMergeGroup(IObject* base, int maxVertices, int polygonMaps=1) = 0;

  /**
   * @brief 標準のからオブジェクトからマージグループを作成します。
   * その後の使い方はcreateMergeGroup(IObject, int)と同じです。
   *
   * @param maxVertices 総合頂点数の最大値。
   * @param priority レンダー優先リスト名
   * これ以上の数の頂点を結合させることは出来ません。
   **/
  virtual IMergeGroup* createMergeGroup(int maxVertices) = 0;

  /**
   * 新しいレンダーキューを作成します。
   * レンダーキューで、描画コールを登録したり、登録したコールを
   * 実行するためのAPIがあります。　レンダーキューにビュー行列、投影行列、
   * 画面クリア設定、フォグ設定などを個別に管理できます。
   **/
  virtual IRenderQueue* createRenderQueue() = 0;

  /**
   * @brief シェダープログラムをロードします
   * vShaderName + '_' + fShaderName がシェーダーを象徴する名前となります。
   * レンダー設定ファイルなどでシェーダーを参照する場合、以上の名前でリンクできます。
   *
   * @param vertexCode 頂点シェダーGLSLコード
   * @param fragmentCode フラグメントシェーダーGLSLコード
   * @param vShaderName 頂点シェーダーを認識する名前 (ファイル名)
   * @param fShaderName フラグメントシェーダーを認識する名前 (ファイル名)
   * @return 成功の場合、これからロードされたシェダーを指すための
   * シェダー番号。　失敗の場合は -1
   **/
  virtual int loadShader(const char* vertexCode, const char* fragmentCode, 
			 const char* vShaderName, const char* fShaderName) = 0;

  /**
   * @brief シェダープログラムをロードします
   * vShaderName + '_' + fShaderName がシェーダーを象徴する名前となります。
   * レンダー設定ファイルなどでシェーダーを参照する場合、以上の名前でリンクできます。
   *
   * @param vertexCode 頂点シェダーGLSLコード
   * @param fragmentCode フラグメントシェーダーGLSLコード
   * @param programName シェーダプログラムの名前
   * @return 成功の場合、これからロードされたシェダーを指すための
   * シェダー番号。　失敗の場合は -1
   **/
  virtual int loadShader(const char* vertexCode, const char* fragmentCode, 
			 const char* programName) = 0;

  /**
   * @brief シェーダーに任意のUniform変数を定義する
   *
   * @param shaderId ロード済みのシェーダーID
   * @param uniformPos 任意の整数・DrawCall時にこれを指定する
   * @param uniformName シェーダーのuniform変数名
   *
   **/
  virtual void addShaderUniform(int shaderId, int uniformPos, const char *uniformName) = 0;

  /**
   * @return シェーダー名に相当するシェーダーのリソースID (存在しなかった場合は-1)
   **/
  virtual int getShaderProgramID(const char* shaderName) = 0;

  /**
   * @return 指定Uniformが指定シェーダに設定されているか
   **/
  virtual bool hasMyUniform( int shaderID, int uniformPos ) = 0;

  /**
   * @brief 画像ファイルのバイトデータからレンダリングで使用できる
   * テクスチャーをロードする。
   *
   * 現在対応中のファイル形式：
   * - .pkm (アンドロイド端末のみ)
   * - .dds (atitc形式のみ、アンドロイド端末の一部のみ)
   * - .ctes(atitc形式のみ、アンドロイド端末の一部のみ)
   * - .pvr (pvrtc圧縮、PowerVRチップセットのみ)
   *
   * - .bmp (32ビットARGB)
   * - .bmp (24ビットRGB)
   * - .bmp (16ビット565)
   * - .bmp (16ビット4444)
   * - .bmp (16ビット1555)
   * - .png
   * - .jpg
   * - .gif
   * - その他、FreeImageライブラリーで対応形式全部
   *
   * @param data バイトデータ(全ファイル、ヘッダー付き)
   * @param length dataの長さ
   * @param repeat リピート設定にする場合はtrue。falseの場合はCLAMP設定になります
   * @param mipmap trueの場合、ミップマップの自動生成/データからロードされることとなります。
   * @param gpuOnly trueの場合、テクスチャをグラフィックメモリに転送後、バイトデータを解放します。
   * 実際の動作は形式によります。
   *
   * @return 成功の場合、新しくロードされたテクスチャーの仮ID。　失敗の場合は-1
   **/
  virtual int loadTexture(byte* data, int length, bool repeat, bool mipmap, bool gpuOnly = true) = 0;

  /**
   * @brief 画像ファイルのバイトデータからキューブテクスチャーをロードします。
   * 次の順番でデータがロードされます：
   * GL_TEXTURE_CUBE_MAP_POSITIVE_X​
   * GL_TEXTURE_CUBE_MAP_NEGATIVE_X​
   * GL_TEXTURE_CUBE_MAP_POSITIVE_Y​
   * GL_TEXTURE_CUBE_MAP_NEGATIVE_Y​
   * GL_TEXTURE_CUBE_MAP_POSITIVE_Z​
   * GL_TEXTURE_CUBE_MAP_NEGATIVE_Z​
   *
   * @param data 画像データ6枚分が入っている配列
   * @param length 画像データの長さ6枚分が入っている配列
   * @param trueの場合、ミップマップの自動生成/データからロードされることとなります。
   *
   **/
  virtual int loadTextureCube(byte* data[6], int length[6], bool mipmap) = 0;

  /**
   * @brief	テクスチャを解放
   * @param	texture	loadTexture()によって返された、テクスチャのID
   */
  virtual void deleteTexture(int texture) = 0;

  /**
   * @return テクスチャ情報
   */
  virtual TextureInfo getTextureInfo(int texture) = 0;

  /**
   * @brief 空のテクスチャーを作成します。
   *
   * @param width 幅
   * @param height 高さ
   * @param format glTexImage2Dが対応する形式
   * @param glTexImage2Dが対応するピクセル形式
   * @param repeat リピート設定にする場合はtrue。falseの場合はCLAMP設定になります
   * @param data (オプショナル)画像バイトデータ(ヘッダー抜き)
   * @param filterLinear (オプショナル)拡縮バイリニアフィルター使用フラグ(falseならフィルターなし)
   * @return 成功の場合、新しくロードされたテクスチャーの仮ID。　失敗の場合は-1
   **/
  virtual int createTexture( int width, int height, int format, int pixelFormat, 
			     bool repeat, byte* data = NULL, bool filterLinear = true ) = 0;

  /**
   * @brief 空のキューブテクスチャーを作成します。
   *
   * @param width 幅
   * @param height 高さ
   * @param format glTexImage2Dが対応する形式
   * @param glTexImage2Dが対応するピクセル形式
   * @param repeat リピート設定にする場合はtrue。falseの場合はCLAMP設定になります
   * @param data (オプショナル)画像バイトデータ(ヘッダー抜き)を6枚
   * @param filterLinear (オプショナル)拡縮バイリニアフィルター使用フラグ(falseならフィルターなし)
   * @return 成功の場合、新しくロードされたテクスチャーの仮ID。　失敗の場合は-1
   **/
  virtual int createTextureCube( int width, int neight, int format, int pixelFormat, 
				 bool repeat, byte* data[6], bool filterLinear = true ) = 0;

  /**
   * @brief 指定テクスチャの指定位置にサブ画像をコピーします
   *
   * @param texture サブ画像コピー先のテクスチャ
   * @param offsetX コピー開始位置X
   * @param offsetY コピー開始位置Y
   * @param width 幅
   * @param height 高さ
   * @param format glTexImage2Dが対応する形式
   * @param pixelFormat glTexImage2Dが対応するピクセル形式
   * @param subData サブ画像のバイトデータ（色情報のみ、ヘッダーなし）
   * @return true-成功 false-テクスチャが存在しないか、コピーに失敗
   **/
  virtual bool copySubImage(int texture, int offsetX, int offsetY, int width, int height, 
			    int format, int pixelFormat, byte *subData) = 0;

  /**
   * @brief FBOを新規作成します
   **/
  virtual int createFBO() = 0;

  /**
   * @brief 指定のFBOのサーフェースとして指定のテクスチャーをバインドします
   *
   * @param fbo FBOリソースID
   * @param texture テクスチャーリソースID
   * @param createDepth レンダーサーフェースが既にFBOにあった場合は、上書きされます。
   **/
  virtual void setFBOTexture(int fbo, int texture, bool createDepth) = 0;

  /**
   * @brief bs/binファイルペアのバイトデータからオブジェクトに適用できる
   * アニメーションをロードする。
   *
   * @param bsFile .bsファイルの文字列データ(=バイトデータ)、JSON形式
   * @param bsFileLength bsFileデータの長さ
   * @param binFile .binファイルのバイトデータ
   * @param binFileLength binFileバイトデータの長さ
   *
   * @return 成功の場合、新しくロードされたアニメーションの仮ID。　失敗の場合は-1
   **/
  virtual int loadAnimation(char* bsFile, int bsFileLength, byte* binFile, int binFileLength) = 0;

  /**
   * @brief bbmファイルペアのバイトデータからオブジェクトに適用できる
   * アニメーションをロードする。
   *
   * @param bbmFile .bbmファイルのバイトデータ
   * @param bbmFileLength bbmFileデータの長さ
   *
   * @return 成功の場合、新しくロードされたアニメーションの仮ID。　失敗の場合は-1
   **/
  virtual int loadAnimation(byte* bbmFile, int bbmFileLength) = 0;


  /**
   * @brief 子アニメーションに親アニメーションを各フレーム掛け合わせます。
   * 親アニメーションに変更はありません。
   * 親、子アニメーションのサイズが別々だった場合、短い方が
   * ループ再生で掛け合わせられます。
   *
   * @param child 子アニメーション、このアニメーションの行列は変えられます
   * @param parent 親アニメーション、このアニメーションは変えられません
   **/
  virtual void setAnimationParent(int child, int parent) = 0;

  /**
   * @brief 指定のアニメーションの関連リソースを開放する
   * @param animationID loadAnimationで取得されたアニメーション番号
   *
   * @return 成功の場合はtrue、指定のIDが存在しない場合はfalse
   **/
  virtual bool deleteAnimation(int animationID) = 0;

  /**
   * mrf(レンダー設定ファイル)ファイルリソースを読み込み、ロードします。
   * @param mrfFile .mrfファイルの文字列データ
   * @param skipSetUniforms 別スレッドでロードした場合はsetUniforms()が失敗するので、これをtrueにし、後でIMrf::setUniforms()を呼ぶ
   * @return 成功の場合、新しくロードされたリソースの仮ID、失敗の場合は-1
   **/
  virtual IMrf* loadMrf(const char* mrfFile, bool skipSetUniforms = false) = 0;

  /**
   * @brief アニメーション再生/操作を行うためのプレイヤーを作成する
   * 注意：このメソッドは、object内の全てのレイヤーが、同じボーン名を共有する前提で
   * 実装されています。 ポリゴンマップによってボーン名が異なる場合は、ポリゴンマップ単位で
   * アニメーションプレイヤーを生成してください。 (@see newAnimationPlayer(int, IPolygonMap*))
   *
   * @param animation loadAnimationメソッドで正常にロードされたアニメーションリソース
   * @param object loadBo3でロードされたObjectリソース。NULLでも可だが、再生前に設定が必要
   * (@see prepareAnimationPlayerForObject(IAnimationPlayer*, IObject*))
   *
   * @return 成功の場合はアニメーションを再生するためのプレイヤー、エラーの場合はNULL
   **/
  virtual IAnimationPlayer* loadAnimationPlayer( int animation, IObject* object) = 0;

  /**
   * @brief アニメーション再生/操作を行うためのプレイヤーを作成する
   *
   * @param animation loadAnimationメソッドで正常にロードされたアニメーションリソース
   * @param polygonmap loadBo3でロードされたObjectリソース内の一つのポリゴンマップ。NULLでも可だが、再生前に設定が必要(@see prepareAnimationPlayerForPolygonMap(IAnimationPlayer*, IPolygonMap*))
   *
   * @return 成功の場合はアニメーションを再生するためのプレイヤー、エラーの場合はNULL
   **/
  virtual IAnimationPlayer* loadAnimationPlayer( int animation, IPolygonMap* polygonMap) = 0;

  /**
   * @brief 指定アニメーションが指定オブジェクトで使用可能にするための設定を行う
   * 注意：このメソッドは、object内の全てのレイヤーが、同じボーン名を共有する前提で
   * 実装されています。 ポリゴンマップによってボーン名が異なる場合は、ポリゴンマップ単位で
   * アニメーションプレイヤーを設定してください。 
   * (@see prepareAnimationPlayerForPolygonMap(IAnimationPlayer*, IPolygonMap*))
   *
   * @param animation loadAnimationメソッドで正常にロードされたアニメーションリソース
   * @param object loadBo3でロードされたObjectリソース
   */
  virtual void adaptTo( IAnimationPlayer *animationPlayer, 
			IObject *object ) = 0;
  
  /**
   * @brief 指定アニメーションが指定ポリゴンマップで使用可能にするための設定を行う
   *
   * @param animation loadAnimationメソッドで正常にロードされたアニメーションリソース
   * @param polygonmap loadBo3でロードされたObjectリソース内の一つのポリゴンマップ
   **/
  virtual void adaptTo( IAnimationPlayer *animationPlayer, IPolygonMap *polygonMap ) = 0;

  /**
   * @brief ビューポートを設定する
   *
   * @param framebuffer FBOの仮ID (-1=画面)
   * @param left 左
   * @param top 上
   * @param right 右
   * @param bottom 下
   **/
  virtual void setViewport(int framebuffer, int left, int top, int right, int bottom) = 0;

  /**
   * @brief ビューポート{ left, top, width, height }
   */
  virtual int* getViewport(int framebuffer) = 0;

  /**
   * @brief フロートベクタータイプのuniform値を設定します。
   * 相当するDrawCallのシェーダーの、指定されたuniformIDの場所に
   * レンダー時に指定の値がロードされます。
   *
   * @param vector ユニフォーム値
   * @param numComponents ベクターのコンポネント数(1-4)
   **/
  virtual MyUniformValue* acquireMyUniform(const float* vector, int numComponents) = 0;

  /**
   * @brief Textureタイプのuniform値を設定します。
   * 相当するDrawCallのシェーダーの、指定されたuniformIDの場所に
   * レンダー時に指定の値がロードされます。
   *
   * @param texture textureID
   * @param GL_TEXTURE1以上の値
   **/
  virtual MyUniformValue* acquireMyUniform(int texture, int glactive) = 0;

  /**
   * @brief Full screen描画登録を行います。 登録された内容が、IGLBase::execRender()が
   * 呼び出されるタイミングで、実際にレンダリングされます。
   *
   * @return drawCall レンダリング内容を格納する構造へのハンドル体。
   * このハンドルを使うことで、レンダー内容を設定することが出来ます。
   **/
  virtual DrawCall* acquireDrawCall() = 0;

  /**
   * @brief 描画登録を行います。 登録された内容が、IGLBase::execRender()が
   * 呼び出されるタイミングで、実際にレンダリングされます。
   *
   * @param polygonMap レンダー登録される対象ポリゴンマップ
   * @return drawCall レンダリング内容を格納する構造へのハンドル体。
   * このハンドルを使うことで、レンダー内容を設定することが出来ます。
   **/
  virtual DrawCall* acquireDrawCall(IPolygonMap* polygonMap) = 0;

  /**
   * @brief 描画登録を行います。 登録された内容が、IGLBase::execRender()が
   * 呼び出されるタイミングで、実際にレンダリングされます。
   *
   * @param bbox レンダー登録される対象bbox
   * @return drawCall レンダリング内容を格納する構造へのハンドル体。
   * このハンドルを使うことで、レンダー内容を設定することが出来ます。
   **/
  virtual DrawCall* acquireDrawCall(float* bbox) = 0;

  /**
   * @brief 描画登録を行います。 登録された内容が、IGLBase::execRender()が
   * 呼び出されるタイミングで、実際にレンダリングされます。
   *
   * @param numParticles レンダー登録される対象particle数
   * @return drawCall レンダリング内容を格納する構造へのハンドル体。
   * このハンドルを使うことで、レンダー内容を設定することが出来ます。
   **/
  virtual DrawCall* acquireDrawCall(int numParticles) = 0;

  /**
   * @brief クリア登録を行います。
   *
   * @param mode クリアモード
   * @param color クリア色
   * @return drawCall レンダリング内容を格納する構造へのハンドル体。
   * このハンドルを使うことで、レンダー内容を設定することが出来ます。
   **/
  virtual DrawCall* acquireDrawCall(RenderEnums::ClearMode mode, float* color) = 0;

  /**
   * @brief 登録されているDrawCallによって、使用された
   * FBOの使用状態配列を取得
   *
   **/
  virtual bool* getFboUsed() = 0;

  /**
   * @brief フレームレンダーの終了をGLBaseに知らせます
   * このメソッドをフレーム毎に一度、レンダー後に呼び出す必要があります
   **/
  virtual void flush() = 0;

  /**
   * @return initializeを呼んでから経過したミリ秒数
   */
  virtual double getTime() = 0;

  /**
   * レジューム時にこれを呼んでください
   */
  virtual void onResume() = 0;

  /**
   * fTimeSecondsを使用している場合は、これを各フレームの先頭で呼ぶ
   */
  virtual void updateTimer( struct timeval &currentTimev ) = 0;

  /**
   * Reset GL bindings
   */
  virtual void resetGLState() = 0;
};
