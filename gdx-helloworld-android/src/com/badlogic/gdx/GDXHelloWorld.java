package com.badlogic.gdx;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.helloworld.HelloWorld;

public class GDXHelloWorld extends AndroidApplication
{	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);            
        initialize( false );
        getGraphics().setRenderListener( new HelloWorld() );
    }
}