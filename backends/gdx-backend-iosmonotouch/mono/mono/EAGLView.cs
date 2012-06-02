using System;

using OpenTK;
using OpenTK.Graphics.ES20;
using GL1 = OpenTK.Graphics.ES11.GL;
using All1 = OpenTK.Graphics.ES11.All;
using OpenTK.Platform.iPhoneOS;

using MonoTouch.Foundation;
using MonoTouch.CoreAnimation;
using MonoTouch.ObjCRuntime;
using MonoTouch.OpenGLES;
using MonoTouch.UIKit;

namespace mono
{
	[Register ("EAGLView")]
	public class EAGLView : iPhoneOSGameView
	{
		[Export("initWithCoder:")]
		public EAGLView (NSCoder coder) : base (coder)
		{
			LayerRetainsBacking = true;
			LayerColorFormat = EAGLColorFormat.RGBA8;
		}
		
		[Export ("layerClass")]
		public static new Class GetLayerClass ()
		{
			return iPhoneOSGameView.GetLayerClass ();
		}
		
		protected override void ConfigureLayer (CAEAGLLayer eaglLayer)
		{
			eaglLayer.Opaque = true;
		}
		
		protected override void CreateFrameBuffer ()
		{
			try {
				ContextRenderingApi = EAGLRenderingAPI.OpenGLES2;
				base.CreateFrameBuffer ();
			} catch (Exception) {
				ContextRenderingApi = EAGLRenderingAPI.OpenGLES1;
				base.CreateFrameBuffer ();
			}
			
			if (ContextRenderingApi == EAGLRenderingAPI.OpenGLES2)
				LoadShaders ();
		}
		
		protected override void DestroyFrameBuffer ()
		{
			base.DestroyFrameBuffer ();
			DestroyShaders ();
		}
		
		#region DisplayLink support
		
		int frameInterval;
		CADisplayLink displayLink;
		
		public bool IsAnimating { get; private set; }
		
		// How many display frames must pass between each time the display link fires.
		public int FrameInterval {
			get {
				return frameInterval;
			}
			set {
				if (value <= 0)
					throw new ArgumentException ();
				frameInterval = value;
				if (IsAnimating) {
					StopAnimating ();
					StartAnimating ();
				}
			}
		}
		
		public void StartAnimating ()
		{
			if (IsAnimating)
				return;
			
			CreateFrameBuffer ();
			CADisplayLink displayLink = UIScreen.MainScreen.CreateDisplayLink (
				this,
				new Selector ("drawFrame")
			);
			displayLink.FrameInterval = frameInterval;
			displayLink.AddToRunLoop (NSRunLoop.Current, NSRunLoop.NSDefaultRunLoopMode);
			this.displayLink = displayLink;
			
			IsAnimating = true;
		}
		
		public void StopAnimating ()
		{
			if (!IsAnimating)
				return;
			displayLink.Invalidate ();
			displayLink = null;
			DestroyFrameBuffer ();
			IsAnimating = false;
		}
		
		[Export ("drawFrame")]
		void DrawFrame ()
		{
			OnRenderFrame (new FrameEventArgs ());
		}
		
		#endregion
		
		static float[] squareVertices = {
			-0.5f, -0.33f,
			0.5f, -0.33f,
			-0.5f,  0.33f,
			0.5f,  0.33f,
		};
		static byte[] squareColors = {
			255, 255,   0, 255,
			0,   255, 255, 255,
			0,     0,   0,   0,
			255,   0, 255, 255,
		};
		static float transY = 0.0f;
		const int UNIFORM_TRANSLATE = 0;
		const int UNIFORM_COUNT = 1;
		int[] uniforms = new int [UNIFORM_COUNT];
		const int ATTRIB_VERTEX = 0;
		const int ATTRIB_COLOR = 1;
		const int ATTRIB_COUNT = 2;
		int program;
		
		protected override void OnRenderFrame (FrameEventArgs e)
		{
			base.OnRenderFrame (e);
			
			MakeCurrent ();
			
			// Replace the implementation of this method to do your own custom drawing.
			GL.ClearColor (0.5f, 0.5f, 0.5f, 1.0f);
			GL.Clear ((int)All.ColorBufferBit);
			
			if (ContextRenderingApi == EAGLRenderingAPI.OpenGLES2) {
				// Use shader program.
				GL.UseProgram (program);
				
				// Update uniform value.
				GL.Uniform1 (uniforms [UNIFORM_TRANSLATE], transY);
				transY += 0.075f;
				
				// Update attribute values.
				GL.VertexAttribPointer (
					ATTRIB_VERTEX,
					2,
					All.Float,
					false,
					0,
					squareVertices
				);
				GL.EnableVertexAttribArray (ATTRIB_VERTEX);
				GL.VertexAttribPointer (
					ATTRIB_COLOR,
					4,
					All.UnsignedByte,
					true,
					0,
					squareColors
				);
				GL.EnableVertexAttribArray (ATTRIB_COLOR);
				
				// Validate program before drawing. This is a good check, but only really necessary in a debug build.
#if DEBUG
				if (!ValidateProgram (program)) {
					Console.WriteLine ("Failed to validate program {0:x}", program);
					return;
				}
#endif
			} else {
				GL1.MatrixMode (All1.Projection);
				GL1.LoadIdentity ();
				GL1.MatrixMode (All1.Modelview);
				GL1.LoadIdentity ();
				GL1.Translate (0.0f, (float)Math.Sin (transY) / 2.0f, 0.0f);
				transY += 0.075f;
				
				GL1.VertexPointer (2, All1.Float, 0, squareVertices);
				GL1.EnableClientState (All1.VertexArray);
				GL1.ColorPointer (4, All1.UnsignedByte, 0, squareColors);
				GL1.EnableClientState (All1.ColorArray);
			}
			
			GL.DrawArrays (All.TriangleStrip, 0, 4);
			
			SwapBuffers ();
		}
		
		bool LoadShaders ()
		{
			int vertShader, fragShader;
			
			// Create shader program.
			program = GL.CreateProgram ();
			
			// Create and compile vertex shader.
			var vertShaderPathname = NSBundle.MainBundle.PathForResource (
				"Shader",
				"vsh"
			);
			if (!CompileShader (All.VertexShader, vertShaderPathname, out vertShader)) {
				Console.WriteLine ("Failed to compile vertex shader");
				return false;
			}
			
			// Create and compile fragment shader.
			var fragShaderPathname = NSBundle.MainBundle.PathForResource (
				"Shader",
				"fsh"
			);
			if (!CompileShader (All.FragmentShader, fragShaderPathname, out fragShader)) {
				Console.WriteLine ("Failed to compile fragment shader");
				return false;
			}
			
			// Attach vertex shader to program.
			GL.AttachShader (program, vertShader);
			
			// Attach fragment shader to program.
			GL.AttachShader (program, fragShader);
			
			// Bind attribute locations.
			// This needs to be done prior to linking.
			GL.BindAttribLocation (program, ATTRIB_VERTEX, "position");
			GL.BindAttribLocation (program, ATTRIB_COLOR, "color");
			
			// Link program.
			if (!LinkProgram (program)) {
				Console.WriteLine ("Failed to link program: {0:x}", program);
				
				if (vertShader != 0)
					GL.DeleteShader (vertShader);
				
				if (fragShader != 0)
					GL.DeleteShader (fragShader);
				
				if (program != 0) {
					GL.DeleteProgram (program);
					program = 0;
				}
				return false;
			}
			
			// Get uniform locations.
			uniforms [UNIFORM_TRANSLATE] = GL.GetUniformLocation (program, "translate");
			
			// Release vertex and fragment shaders.
			if (vertShader != 0) {
				GL.DetachShader (program, vertShader);
				GL.DeleteShader (vertShader);
			}
				
			if (fragShader != 0) {
				GL.DetachShader (program, fragShader);
				GL.DeleteShader (fragShader);
			}
			
			return true;
		}
		
		void DestroyShaders ()
		{
			if (program != 0) {
				GL.DeleteProgram (program);
				program = 0;
			}
		}
		
		#region Shader utilities
		
		static bool CompileShader (All type, string file, out int shader)
		{
			string src = System.IO.File.ReadAllText (file);
			shader = GL.CreateShader (type);
			GL.ShaderSource (shader, 1, new string[] { src }, (int[])null);
			GL.CompileShader (shader);
			
#if DEBUG
			int logLength = 0;
			GL.GetShader (shader, All.InfoLogLength, ref logLength);
			if (logLength > 0) {
				var infoLog = new System.Text.StringBuilder ();
				GL.GetShaderInfoLog (shader, logLength, ref logLength, infoLog);
				Console.WriteLine ("Shader compile log:\n{0}", infoLog);
			}
#endif
			int status = 0;
			GL.GetShader (shader, All.CompileStatus, ref status);
			if (status == 0) {
				GL.DeleteShader (shader);
				return false;
			}
			
			return true;
		}
		
		static bool LinkProgram (int prog)
		{
			GL.LinkProgram (prog);
			
#if DEBUG
			int logLength = 0;
			GL.GetProgram (prog, All.InfoLogLength, ref logLength);
			if (logLength > 0) {
				var infoLog = new System.Text.StringBuilder ();
				GL.GetProgramInfoLog (prog, logLength, ref logLength, infoLog);
				Console.WriteLine ("Program link log:\n{0}", infoLog);
			}
#endif
			int status = 0;
			GL.GetProgram (prog, All.LinkStatus, ref status);
			if (status == 0)
				return false;
			
			return true;
		}
		
		static bool ValidateProgram (int prog)
		{
			GL.ValidateProgram (prog);
			
			int logLength = 0;
			GL.GetProgram (prog, All.InfoLogLength, ref logLength);
			if (logLength > 0) {
				var infoLog = new System.Text.StringBuilder ();
				GL.GetProgramInfoLog (prog, logLength, ref logLength, infoLog);
				Console.WriteLine ("Program validate log:\n{0}", infoLog);
			}
			
			int status = 0;
			GL.GetProgram (prog, All.LinkStatus, ref status);
			if (status == 0)
				return false;
			
			return true;
		}
		
		#endregion
	}
}
