package com.widget.floatingwidget;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FloatingWidgetAccessibilityService extends AccessibilityService  {
    FrameLayout mLayout;
    WindowManager  windowManager ;
    View botonesView;
    AccessibilityNodeInfo active_nodeInfo;
    int last_event_type=0;
    String last_event_package="";
    String last_event_class="";
    private SpeechRecognizer speech;
    private FloatingWidgetAccessibilityService.recognitionListener rgs;
    int veces=1;
    WindowManager.LayoutParams params ;
    boolean numlock = false;
    boolean statusViewinScreen = true;
    Intent voice;
    WindowManager.LayoutParams lp;
    String command="";
    String numero_reconocido="";
    int status=-1;
    int contador_paginas=0;
    TimerTask myTimerTask;
    Timer timer;
    final Handler handler = new Handler();
    boolean esperar = true;


    @Override
    protected void onServiceConnected() {
        mLayout = new FrameLayout(this);
        botonesView = LayoutInflater.from(this).inflate(R.layout.action_bar, mLayout);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point max_pantalla = new Point();
        windowManager.getDefaultDisplay().getSize(max_pantalla);

        lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        lp.x=0;
        lp.y=max_pantalla.y;

        windowManager.addView(botonesView, lp);

        speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        rgs = new recognitionListener();
        speech.setRecognitionListener((RecognitionListener) rgs);

        voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
                .getPackage().getName());
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);


        botonesView.findViewById(R.id.pag_siguiente).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasa_pagina("siguiente");
            }
        });
        botonesView.findViewById(R.id.pag_anterior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasa_pagina("anterior");
            }
        });
        botonesView.findViewById(R.id.boton_ir_a).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SuperAbuela", "startListening");
                speech.startListening(voice);

                /*pasa_pagina("click");
                if (active_nodeInfo!=null)
                {
                    active_nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }*/
            }
        });

        //inicializa la Activity de Permisos
        Intent intentup = new Intent(this, RuntimePermissionForUser.class);
        intentup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentup);
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            /*TextView myVolumeTextView = botonesView.findViewById(R.id.debug_text);
            String fg = (String) myVolumeTextView.getText();
            myVolumeTextView.setText(fg+":" + event.getKeyCode());
            Log.i("SuperAbuela", "Key pressed via accessibility is: " + event.getKeyCode());*/
            //This allows the key pressed to function normally after it has been used by your app.
            switch (event.getKeyCode()) {
                case 143:
                    //tecla NumLock
                    numlock = (numlock) ? false : true;
                    break;
                case 148:
                    if (!numlock) {
                        pasa_pagina("anterior");
                    }
                    break;
                case 150:
                    if (!numlock) {
                        pasa_pagina("siguiente");
                    }
                    break;
                case 160:
                    Log.i("SuperAbuela", "startListening");
                    speech.startListening(voice);
                    break;
                case 154:
                    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    Point max_pantalla = new Point();
                    windowManager.getDefaultDisplay().getSize(max_pantalla);
                    if (statusViewinScreen) {
                        lp.y = max_pantalla.y;
                    } else {
                        lp.y = 0;
                    }
                    windowManager.updateViewLayout(botonesView, lp);
                    statusViewinScreen = (statusViewinScreen) ? false : true;
                    break;
            }
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
         | event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
                //| event.getEventType()==AccessibilityEvent.TYPE_VIEW_SELECTED
                //| event.getEventType()==AccessibilityEvent.TYPE_VIEW_FOCUSED
        ) {*/
            if (true) {
                AccessibilityNodeInfo nodeInfo = event.getSource();
                List<AccessibilityWindowInfo> currentNode=null; //getWindows();
                //nodeInfo=findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
                if (currentNode !=null)
                    if (currentNode.get(0)!=null)
                        nodeInfo=currentNode.get(0).getRoot();
                /*Log.i("SuperAbuela", "onAccessibilityEvent=" + event.getEventType() + "Package=" + event.getPackageName()+" isEnabled="+event.isEnabled());
                if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                    Log.i("SuperAbuela", "------Tipo_Cambio=" + event.getContentChangeTypes());
                logViewHierarchy(nodeInfo, 0);
                Log.i("SuperAbuela", "Encontrado Pantalla Menú:" + isPagina("menu",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla Thumbnail:" + isPagina("thumbnail",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla Pop-up Ir a:" + isPagina("popup-ira",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla lectura:" + isPagina("lectura",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla Biblioteca:" + isPagina("biblioteca",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla seekBar:" + isPagina("seekBar",nodeInfo,(String) event.getPackageName(),event.getEventType(),(String) event.getClassName()));*/
                Log.i("SuperAbuela", "onAccessibilityEvent=" + event.getEventType() + "Package=" + event.getPackageName()+" isEnabled="+event.isEnabled());
                Log.i("SuperAbuela", "=================================================");
                logViewHierarchy(nodeInfo, 0);
                if (nodeInfo == null) {
                    return;
                } else {
                    active_nodeInfo = nodeInfo;
                    last_event_type = event.getEventType();
                    last_event_package = (String) event.getPackageName();
                    last_event_class = (String) event.getClassName();
                }
            if (!esperar)
                process_comando();
                //nodeInfo.recycle();
        }
    }

    @Override
    public void onInterrupt() {

    }

    private class recognitionListener implements RecognitionListener {
        @Override
        public void onResults(Bundle data)
        {
            String commando = "";
            numero_reconocido="";

            veces=1;
            ArrayList<String> matches = data.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);

            matches.forEach(e -> {
                Log.i("SuperAbuela", "Reconocido: " + e); });

            for(String element: matches){
                String[] palabras = element.split(" ");
                for(String palabra: palabras){
                    String numero = palabra.replaceAll("[^\\d]", "");
                    if (!"".equals(numero)) {
                        veces = Integer.parseInt(numero);
                        numero_reconocido=numero;
                    }
                    if ("siguiente".equals(palabra)) commando="siguiente";
                    if ("siguientes".equals(palabra)) commando="siguiente";
                    if ("adelante".equals(palabra)) commando="siguiente";
                    if ("anterior".equals(palabra)) commando="anterior";
                    if ("anteriores".equals(palabra)) commando="anterior";
                    if ("atras".equals(palabra)) commando="anterior";
                    if ("ir".equals(palabra)) commando="ira";
                }
            }

            if (veces==0) veces=1;
            //for(int i=1;i<=repeticion;i++) {
            if ("siguiente".equals(commando)) pasa_pagina("siguiente");
            if ("anterior".equals(commando)) pasa_pagina("anterior");
            if ("ira".equals(commando)) {
                command = commando;
                status =0;
                process_comando();
            }
            //}

        }

        @Override
        public void onBeginningOfSpeech()
        {
            Log.i("SuperAbuela", "onBeginningOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] arg0)
        {
        }


        @Override
        public void onEndOfSpeech()
        {
            Log.i("SuperAbuela", "onEndOfSpeech");
        }


        @Override
        public void onError(int e)
        {
        }


        @Override
        public void onEvent(int arg0, Bundle arg1)
        {
        }


        @Override
        public void onPartialResults(Bundle arg0)
        {
            ArrayList<String> matches = arg0.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            matches.forEach(e -> {
                Log.i("SuperAbuela", "PartialResults: " + e);
            });
        }


        @Override
        public void onReadyForSpeech(Bundle arg0)
        {
            Log.i("SuperAbuela", "onReadyForSpeech");
        }


        @Override
        public void onRmsChanged(float arg0)
        {
        }
    }

    public static void logViewHierarchy(AccessibilityNodeInfo nodeInfo, final int depth) {

        if (nodeInfo == null) return;

        String spacerString = "";

        for (int i = 0; i < depth; ++i) {
            spacerString += '-';
        }
        //Log the info you care about here... I choce classname and view resource name, because they are simple, but interesting.
        Log.i("SuperAbuela", "H:"+spacerString + nodeInfo.getClassName() + "," + nodeInfo.isClickable() + "," + nodeInfo.getViewIdResourceName()+","+nodeInfo.getContentDescription()+","+nodeInfo.getText());

        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            logViewHierarchy(nodeInfo.getChild(i), depth+1);
        }
    }
    public void pasa_pagina(String direccion) {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point max_pantalla = new Point();
        windowManager.getDefaultDisplay().getSize(max_pantalla);
        Path movimiento = new Path();
        int duration=500;
        switch (direccion) {
            case  "siguiente":
                movimiento.moveTo(max_pantalla.x/2+100,max_pantalla.y/2);
                movimiento.lineTo(1,max_pantalla.y/2);
                break;
            case  "anterior":
                movimiento.moveTo(max_pantalla.x/2-100,max_pantalla.y/2);
                movimiento.lineTo(max_pantalla.x-1,max_pantalla.y/2);
                break;
            case "click":
                movimiento.moveTo(max_pantalla.x/2,max_pantalla.y/2);
                duration=10;
        }
        GestureDescription.Builder gesto = new GestureDescription.Builder();
        gesto.addStroke(new GestureDescription.StrokeDescription(movimiento,0,duration));
        dispatchGesture(gesto.build(),new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                if (veces>=100) veces=1; // se puede reconocer como numero una hora 01:00, está se convierte en 100
                veces = veces -1;
                if (veces>0) pasa_pagina(direccion);
                super.onCompleted(gestureDescription);
            }
        },null);
    }
    public boolean isPagina (String pantalla, AccessibilityNodeInfo nodeInfo, String packageName, int eventType, String className) {
        boolean encontrado = false;
        boolean seguir = true;
        if (nodeInfo != null) {
            switch (pantalla) {
                case "lectura":
                    if ("com.amazon.krf.platform.KRFView".equals(nodeInfo.getClassName()) && "com.amazon.kindle".equals(packageName) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                        encontrado = true;
                    }
                    break;
                case "thumbnail":
                    if ("Abrir menú lateral.".equals(nodeInfo.getContentDescription()) && "com.amazon.kindle".equals(packageName) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                        encontrado = true;
                    }
                    break;
                case "menu":
                    if ("Ir a".equals(nodeInfo.getText()) && "com.amazon.kindle".equals(packageName) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                        encontrado = true;
                    }
                    break;
                case "popup-ira":
                    if (nodeInfo.getText()!=null)
                        if (nodeInfo.getText().length()>=19) {
                            if ("Introduce la página".equals(nodeInfo.getText().toString().substring(0,19)) && "com.amazon.kindle".equals(packageName) && eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
                                encontrado = true;
                        }
                    break;
                case "biblioteca":
                    if (nodeInfo.getText()!=null)
                        //if (nodeInfo.getText().length()==16) {
                            if ("De su biblioteca".equals(nodeInfo.getText()) && "com.amazon.kindle".equals(packageName) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED))
                                encontrado = true;
                        //}
                    break;
                case "seekBar":
                    seguir=false;
                    if ("com.amazon.kindle".equals(packageName) && "android.widget.SeekBar".equals(className)) {
                        encontrado = true;
                    }
                    break;
            }
            if (!encontrado && seguir)
                for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                    encontrado=isPagina(pantalla,nodeInfo.getChild(i),packageName,eventType,className);
                    if (encontrado) break;
            }
        }
        return encontrado;
    }
    public AccessibilityNodeInfo getNodeInfo (String control, AccessibilityNodeInfo nodeInfo) {
        AccessibilityNodeInfo node=null;
        if (nodeInfo != null) {
            switch (control) {
                case "":
                    break;
                case "hamburger":
                    if ("Abrir menú lateral.".equals(nodeInfo.getContentDescription()) ) {
                        node = nodeInfo;
                    }
                    break;
                case "ira":
                    if ("Ir a".equals(nodeInfo.getText()) ) {
                        node = nodeInfo;
                    }
                    break;
                case "pagina":
                    if ("PÁGINA".equals(nodeInfo.getText()) ) {
                        node = nodeInfo;
                    }
                    break;
                case "edit-pagina":
                    if ("android.widget.EditText".equals(nodeInfo.getClassName()) ) {
                        node = nodeInfo;
                    }
                    break;
            }
            if (node==null)
                for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                    node=getNodeInfo(control,nodeInfo.getChild(i));
                    if (node!=null) break;
                }
        }
        if (node!=null)
            Log.i("SuperAbuela", "getNodeInfo_Locate:"+node.getClassName() + "," + node.isClickable() + "," + node.getViewIdResourceName()+","+node.getContentDescription()+","+node.getText());
        return node;
    }
    public void process_comando() {
        Log.i("SuperAbuela", "Process_Command Lanzado-Comando:"+command+" Estado:" + status);
        Log.i("SuperAbuela", "onAccessibilityEvent=" + last_event_type + "Package=" + last_event_package+" ClassName="+last_event_class);
        logViewHierarchy(active_nodeInfo, 0);
        switch (command) {
            case "ira":
                Log.i("SuperAbuela", "Comando: ira" );
                switch (status) {
                    case 0:
                        Log.i("SuperAbuela", "Estado: 0" );
                        delay(2000);
                        esperar=true;
                        status=1;
                        contador_paginas=0;
                        pasa_pagina("click");
                        break;
                    case 1:
                        Log.i("SuperAbuela", "Estado: 1" );
                        process_task("thumbnail",1,"hamburger",null, "",false, 2,true);
                        break;
                    case 2:
                        Log.i("SuperAbuela", "Estado: 2" );
                        process_task("menu",1,"ira",null,"",false, 3,true);
                        break;
                    case 3:
                        Log.i("SuperAbuela", "Estado: 3" );
                        delay(2000);
                        process_task("popup-ira",1,"pagina","edit-pagina",numero_reconocido,true,4,true);
                        break;
                    case 4:
                        Log.i("SuperAbuela", "Estado: 4" );
                        process_task("anyScreen",1,null,null,numero_reconocido,false, -1,false);
                        break;
                    case 5:
                        Log.i("SuperAbuela", "Estado: 5" );
                        process_task("lectura",1,null,null,numero_reconocido,false,-1,false);
                        break;
                }
                break;
        }
    }

    public void process_task(String pantalla,int veces_sale_pantalla,String control,String control2,String numero, boolean espera_siguiente_estado,int siguiente_estado, boolean condicion_positiva) {
        if (isPagina(pantalla,active_nodeInfo,last_event_package,last_event_type,last_event_class)) {
            contador_paginas = contador_paginas +1;
            if (contador_paginas==veces_sale_pantalla) {
                if (control!=null) {
                    AccessibilityNodeInfo nodeInfo_control = getNodeInfo(control,active_nodeInfo);
                    if (nodeInfo_control!=null) {
                        if (control2!=null) {
                            AccessibilityNodeInfo nodeInfo_control2 = getNodeInfo(control2,active_nodeInfo);
                            if (nodeInfo_control2!=null) {
                                Bundle arguments = new Bundle();
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                        numero);
                                nodeInfo_control2.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            }
                        }
                        status=siguiente_estado;
                        contador_paginas=0;
                        esperar = espera_siguiente_estado;
                        if (condicion_positiva) {
                            nodeInfo_control.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                } else {
                    // Tap
                    status = siguiente_estado;
                    contador_paginas = 0;
                    if (condicion_positiva) {
                        pasa_pagina("click");
                        Log.i("SuperAbuela", "Tap");
                    }
                }
            }
        } else {
            Log.i("SuperAbuela", "Pantalla no detectada:" + pantalla);
            if (!condicion_positiva) {
                //status = siguiente_estado;
                contador_paginas = 0;
                pasa_pagina("click");
                //nodeInfo_control.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i("SuperAbuela", "Tap");
            }
        }
    }
    public void delay (int ms) {
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                process_comando();
            }
        }, ms);
    }
}


