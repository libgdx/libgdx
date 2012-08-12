package com.badlogic.gdx.utils;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

public final class D {
	
	public static final int MB = 1;
	public static final int GB = 2;
	public static final int KB = 3;
	public static final int B = 4;
	
	
	public static float DELAY = 1.3f;
	
	private static float timer[] = new float[]{-1,-1,-1,-1,-1};
	
	private D(){
	}
	
	/********************************************************************
	 * 
	 ********************************************************************/
	public static void log(String logTitle,String logInfo){
		timer[0] += Gdx.graphics.getDeltaTime();
		if(timer[0] >= DELAY){
			Gdx.app.log("**Debug log** : " + logTitle + " ", " " + logInfo);
			timer[0] = 0;
		}
	}
	
	public static void log1(String logTitle,String logInfo){
		timer[1] += Gdx.graphics.getDeltaTime();
		if(timer[1] >= DELAY){
			Gdx.app.log("**Debug log1** : " + logTitle + " ", " " + logInfo);
			timer[1] = 0;
		}
	}
	public static void log2(String logTitle,String logInfo){
		timer[2] += Gdx.graphics.getDeltaTime();
		if(timer[2] >= DELAY){
			Gdx.app.log("**Debug log2** : " + logTitle + " ", " " + logInfo);
			timer[2] = 0;
		}
	}
	public static void log3(String logTitle,String logInfo){
		timer[3] += Gdx.graphics.getDeltaTime();
		if(timer[3] >= DELAY){
			Gdx.app.log("**Debug log3** : " + logTitle + " ", " " + logInfo);
			timer[3] = 0;
		}
	}
	
	public static void log4(String logTitle,String logInfo){
		timer[4] += Gdx.graphics.getDeltaTime();
		if(timer[4] >= DELAY){
			Gdx.app.log("**Debug log4** : " + logTitle + " ", " " + logInfo);
			timer[4] = 0;
		}
	}

	/********************************************************************
	 * 
	 ********************************************************************/
	
	public static void out(String string){
		Gdx.app.log("**Print Out** ", " " + string);
	}

	public static void out(int i){
		Gdx.app.log("**Print Out** ", " " + i);
	}

	public static void out(float f){
		Gdx.app.log("**Print Out** ", " " + f);
	}

	public static void out(double d){
		Gdx.app.log("**Print Out** ", " " + d);
	}
	
	public static void out(boolean b){
		Gdx.app.log("**Print Out** ", " " + b);
	}
	
	public static void out(long l){
		Gdx.app.log("**Print Out** ", " " + l);
	}
	/********************************************************************
	 * 
	 ********************************************************************/

	public static void out(String title,int[] i){
		int leng = i.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(i[j]);
		}
		Gdx.app.log("**Print Out** ",title +  " : " + tmp.toString());
	}
	
	public static void out(String title,float[] f){
		int leng = f.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(f[j]);
		}

		Gdx.app.log("**Print Out** ",title +  " : " + tmp.toString());
	}
	
	public static void out(String title,double[] d){
		int leng = d.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(d[j]);
		}

		Gdx.app.log("**Print Out** ",title +  " : " + tmp.toString());
	}
	
	public static void out(String title,boolean[] b){
		int leng = b.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(b[j]);
		}

		Gdx.app.log("**Print Out** ",title +  " : " + tmp.toString());
	}
	
	public static void out(String title,long[] l){
		int leng = l.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(l[j]);
		}
		Gdx.app.log("**Print Out** ",title +  " : " + tmp.toString());
	}

	/********************************************************************
	 * 
	 ********************************************************************/

	public static void out(int[] i){
		int leng = i.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(i[j]);
		}
		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	
	public static void out(float[] f){
		int leng = f.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append("   ");
			tmp.append(f[j]);
		}

		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	
	public static void out(double[] d){
		int leng = d.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(d[j]);
		}

		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	
	public static void out(boolean[] b){
		int leng = b.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(b[j]);
		}

		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	
	public static void out(long[] l){
		int leng = l.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(l[j]);
		}
		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}

	public static void out(byte[] l){
		int leng = l.length;
		StringBuilder tmp = new StringBuilder();
		for(int j = 0; j <  leng;j++){
			tmp.append(" ");
			tmp.append(l[j]);
		}
		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	/********************************************************************
	 * 
	 ********************************************************************/

	public static void out(Iterator<Integer> i){
		StringBuilder tmp = new StringBuilder();
		while(i.hasNext()){
			tmp.append(" ");
			tmp.append(i.next());
		}
		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	
	public static void out(Iterator<String> i,int t){
		StringBuilder tmp = new StringBuilder();
		while(i.hasNext()){
			tmp.append("   ");
			tmp.append(i.next());
		}
		Gdx.app.log("**Print Out** ", " " + tmp.toString());
	}
	
	
	
	/********************************************************************
	 * 
	 ********************************************************************/

	public static void outln(String string){
		
		Gdx.app.log("**Print Out** ", " \n" + string);
	}

	public static void outln(int i){
		Gdx.app.log("**Print Out** ", " 'n" + i);
	}

	public static void outln(float f){
		Gdx.app.log("**Print Out** ", " \n" + f);
	}

	public static void outln(double d){
		Gdx.app.log("**Print Out** ", " \n" + d);
	}

	public static void outln(long l){
		Gdx.app.log("**Print Out** ", " \n" + l);
	}
	
	public static void outln(boolean b){
		Gdx.app.log("**Print Out** ", " \n" + b);
	}
	/********************************************************************
	 * 
	 ********************************************************************/
	
	public static void ln(){
		Gdx.app.log("", "\n");
	}

	/********************************************************************
	 * 
	 ********************************************************************/
	
	public static void out(Vector2... v){
		if(v[0] == null)
			D.out("NULL");
		StringBuilder tmp = new StringBuilder();
		for(int i = 0;i < v.length;i++){
			tmp.append("\n Vector2["+ i + "]  : " + v[i].x + "   " + v[i].y );
		}
		D.out(tmp.toString());
	}
	
	/********************************************************************
	 * 
	 ********************************************************************/
	
	public static void nativeHeap(){
		Gdx.app.log("Native Heap : ", "" + Gdx.app.getNativeHeap());
	}
	
	public static void javaHeap(int type){
		switch (type) {
			case MB:
				Gdx.app.log("Java Heap   : ", "" + Gdx.app.getJavaHeap()/1024/1024);
				break;
			case GB:
				Gdx.app.log("Java Heap   : ", "" + Gdx.app.getJavaHeap()/1024/1024/1024);
				break;
			case KB:
				Gdx.app.log("Java Heap   : ", "" + Gdx.app.getJavaHeap()/1024);
				break;

			default:
				Gdx.app.log("Java Heap   : ", "" + Gdx.app.getJavaHeap());
				break;
		}
	}
	
	public static long jHeap(int type){
		switch (type) {
			case MB:
				return Gdx.app.getJavaHeap()/1024/1024;
			case KB:
				return Gdx.app.getJavaHeap()/1024;
			case GB:
				return Gdx.app.getJavaHeap()/1024l/1024l/1024l;
		}
		return Gdx.app.getJavaHeap();
	}

	public static long nHeap(int type){
		switch (type) {
			case MB:
				return Gdx.app.getNativeHeap()/1024/1024;
			case KB:
				return Gdx.app.getNativeHeap()/1024;
			case GB:
				return Gdx.app.getNativeHeap()/1024l/1024l/1024l;
		}
		return Gdx.app.getNativeHeap();
	}

	public static void heap(int id){
		Gdx.app.log("Heap " + id +" ", "Java Heap:  " + Gdx.app.getJavaHeap() + "   Native Heap:  " + Gdx.app.getNativeHeap());
	}
}

