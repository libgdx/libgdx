package com.badlogic.gdx2.backends.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.badlogic.gdx2.Application;
import com.badlogic.gdx2.Graphics;
import com.badlogic.gdx2.RenderListener;
import com.badlogic.gdx2.graphics.Font;
import com.badlogic.gdx2.graphics.GL10;
import com.badlogic.gdx2.graphics.GL11;
import com.badlogic.gdx2.graphics.GL20;
import com.badlogic.gdx2.graphics.Pixmap;
import com.badlogic.gdx2.graphics.ShaderProgram;
import com.badlogic.gdx2.graphics.Texture;
import com.badlogic.gdx2.graphics.Font.FontStyle;
import com.badlogic.gdx2.graphics.Pixmap.Format;
import com.badlogic.gdx2.graphics.Texture.TextureFilter;
import com.badlogic.gdx2.graphics.Texture.TextureWrap;
import com.badlogic.gdx2.math.WindowedMean;

public class JoglGraphics implements Graphics, RenderListener
{
	/** the jframe **/
	private final JFrame frame;
	
	/** the graphic panel **/	
	protected final JoglPanel graphicPanel;
	
	/** the render listener **/
	private RenderListener listener;	
	
	/** GL11 instance **/
	private GL11 gl11;
	
	/** GL20 instance **/
	private GL20 gl20;
	
	/** wheter to use opengl 2 **/
	private final boolean useGL2;
		
	/** the last frame time **/
	private long lastFrameTime = System.nanoTime();
		
	/** the deltaTime **/
	private float deltaTime = 0;
	
	/** the deltaTime mean **/
	private WindowedMean mean = new WindowedMean( 5 );
	
	JoglGraphics( final JoglApplication application, String title, int width, int height, boolean useGL2IfAvailable )
	{		
		frame = new JFrame( title );
		graphicPanel = new JoglPanel( application );        
        graphicPanel.setPreferredSize( new Dimension( width, height ) );
        frame.setSize( width + frame.getInsets().left + frame.getInsets().right, frame.getInsets().top + frame.getInsets().bottom + height );
        frame.add(graphicPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible( true );                
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {                 	                	
            	graphicPanel.dispose(); 
            	if( application.listener != null )
            		application.listener.destroy();
            }
        });                      
        useGL2 = useGL2IfAvailable;
        graphicPanel.addGraphicListener( this );
	}

	@Override
	public GL10 getGL10() 
	{	
		return gl11;
	}

	@Override
	public GL11 getGL11() 
	{	
		return gl11;
	}

	@Override
	public GL20 getGL20() 
	{	
		return gl20;
	}

	@Override
	public int getHeight() 
	{	
		return graphicPanel.getWidth();
	}

	@Override
	public int getWidth() 
	{	
		return graphicPanel.getHeight();
	}

	@Override
	public boolean isGL11Available() 
	{	
		return gl11 != null;
	}

	@Override
	public boolean isGL20Available() 
	{	
		return gl20 != null;
	}

	@Override
	public Font newFont(String fontName, int size, FontStyle style) 
	{	
		return new JoglFont( this, fontName, size, style );
	}

	@Override
	public Font newFont(InputStream inputStream, int size, FontStyle style) 
	{			
		return new JoglFont( this, inputStream, size, style );
	}

	@Override
	public Pixmap newPixmap(int width, int height, Format format) 
	{	
		return new JoglPixmap( width, height, format );
	}

	@Override
	public Pixmap newPixmap(InputStream in, Format formatHint) 
	{	
		try
		{
			BufferedImage img = (BufferedImage)ImageIO.read( in );
			return new JoglPixmap( img );
		}
		catch( Exception ex )
		{
			return null;
		}		
	}

	@Override
	public Pixmap newPixmap(Object nativePixmap) 
	{	
		return new JoglPixmap( (BufferedImage)nativePixmap );
	}

	@Override
	public ShaderProgram newShaderProgram(String vertexShader, String fragmentShader) 
	{	
		throw new UnsupportedOperationException( "not implemented yet" ); // FIXME
	}

	@Override
	public Texture newTexture(int width, int height, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) 
	{	
		return new JoglTexture( width, height, minFilter, magFilter, uWrap, vWrap );
	}

	@Override
	public Texture newTexture(Pixmap pixmap, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) 
	{
		return new JoglTexture( (BufferedImage)pixmap.getNativePixmap(), minFilter, magFilter, uWrap, vWrap );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRenderListener( RenderListener listener) 
	{	
		if( this.listener != null )
			graphicPanel.removeGraphicListener( this.listener );
		graphicPanel.addGraphicListener(listener);
		this.listener = listener;
	}

	@Override
	public void dispose(Application app) 
	{	
		if( listener != null )
			listener.dispose( app );
	}

	@Override
	public void render(Application app) 
	{			
		// calculate delta time
		deltaTime = ( System.nanoTime() - lastFrameTime ) / 1000000000.0f;
		lastFrameTime = System.nanoTime();
		mean.addValue( deltaTime );			
	}

	@Override
	public void setup(Application app) 
	{
		if( useGL2 )		
			// FIXME add check wheter gl 2.0 is supported
			gl20 = new JoglGL20( graphicPanel.getGL() );		
		else		
			gl11 = new JoglGL11( graphicPanel.getGL() );					
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getDeltaTime() 
	{
		return mean.getMean() == 0?deltaTime:mean.getMean();
	}
}
