package com.badlogic.gdx.tests.lwjgl;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class LocalLwjglTest implements ApplicationListener
{
    private Mesh mesh;

    @Override
    public void create() 
    {
    	float[] vertices = new float[]  { 0, 0, 0, 			//point 0 (invisible)
    									  -0.5f, -0.5f, 0, 	//point 1
    									  0.5f, -0.5f, 0,	//point 2
    									  0, 0.5f, 0};   	//point 3
    	
    	if (mesh == null) 
        {
            mesh = new Mesh(true, 3, 3, new VertexAttribute(Usage.Position, 3, "a_position"));
            mesh.setVertices(vertices,3,3*3); //Heres the problem, the offset does NOT work as expected
            mesh.setIndices(new short[] { 0, 1, 2 });
            
//            Get the vertices again and see whats in them
            float[] testv = new float[12]; 
            mesh.getVertices(testv);
            
            int i = 0;
            while(i < testv.length)
            {
            	System.out.print(i+":"+testv[i++]);
                System.out.print(" "+i+":"+testv[i++]);
            	System.out.print(" "+i+":"+testv[i++]+"\n");
            }
        }
    }

    @Override
    public void dispose() { }

    @Override
    public void pause() { }

    @Override
    public void render() {
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        Gdx.gl10.glColor4f(1, 1, 1, 1);
        mesh.render(GL10.GL_TRIANGLES);
    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void resume() { }
    
    public static void main(String[] argv) {
    	new LwjglApplication(new LocalLwjglTest(), "test", 480, 320, false);
    }
}
