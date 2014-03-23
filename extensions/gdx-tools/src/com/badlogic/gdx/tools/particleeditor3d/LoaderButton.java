package com.badlogic.gdx.tools.particleeditor3d;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;

public abstract class LoaderButton<T> extends JButton{

	public static class ParticleEffectLoaderButton extends LoaderButton<ParticleEffect>{
		public ParticleEffectLoaderButton (ParticleEditor3D editor) {
			this(editor, null);
		}
		public ParticleEffectLoaderButton (ParticleEditor3D editor, Listener<ParticleEffect> listener) {
			super(editor, "Load Controller", listener);
		}
		
		protected void loadResource () {
			File file = editor.showFileLoadDialog();
			if(file != null){
				try{
					String resource = file.getAbsolutePath();
					listener.onResourceLoaded(editor.openEffect(file, false));
				} catch (Exception ex) {
					System.out.println("Error loading effect: " + file.getAbsolutePath());
					ex.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Error opening effect.");
					return;
				}
			}
		}
	}
	
	public static class ModelLoaderButton extends LoaderButton<Model>{
		public ModelLoaderButton (ParticleEditor3D editor) {
			this(editor, null);
		}
		public ModelLoaderButton (ParticleEditor3D editor, Listener<Model> listener) {
			super(editor, "Load Model", listener);
		}
		
		protected void loadResource () {
			File file = editor.showFileLoadDialog();
			if(file != null){
				try{
					String resource = file.getAbsolutePath();
					ModelLoader modelLoader = null;
					if(resource.endsWith(".obj")){
						modelLoader = new ObjLoader(new AbsoluteFileHandleResolver());
					}
					else if(resource.endsWith(".g3dj")){
						modelLoader = new G3dModelLoader(new JsonReader(), new AbsoluteFileHandleResolver());
					}
					else if(resource.endsWith(".g3db")){
						modelLoader = new G3dModelLoader(new UBJsonReader(), new AbsoluteFileHandleResolver());
					}
					else throw new Exception();
					listener.onResourceLoaded(editor.load(resource, Model.class, modelLoader, null));

				} catch (Exception ex) {
					System.out.println("Error loading model: " + file.getAbsolutePath());
					ex.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Error opening effect.");
					return;
				}
			}
		}
	}
	
	
	public interface Listener<T>{
		void onResourceLoaded (T resource);
	}
	
	private String lastDir;
	protected Listener<T> listener;
	ParticleEditor3D editor;
	
	public LoaderButton (ParticleEditor3D editor, String text, Listener<T> listener) {
		super(text);
		this.editor = editor;
		this.listener = listener;
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				loadResource();
			}
		});
	}
	
	public LoaderButton (ParticleEditor3D editor, String text){
		this(editor, text, null);
	}
	
	public void setListener(Listener listener){
		this.listener = listener;
	}
	

	protected abstract void loadResource ();

}
