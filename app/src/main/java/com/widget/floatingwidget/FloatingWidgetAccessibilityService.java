package com.widget.floatingwidget;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
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
    boolean esperar = false;
    String[] pantalla_esperando = {};
    String package_kindle = "com.amazon.kindle";
    int reintentos=0;
    String text_readed ="";
    String title_conf ="";
    int duration = Toast.LENGTH_SHORT;
    boolean hay_pantalla=false;



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

        //Hacer visible los botones
        //windowManager.addView(botonesView, lp);
		//hay_pantalla=true;

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
                //launchApp(package_kindle);
                inicializa_comando("open-app");
                process_comando();
                //pasa_pagina("siguiente",true);
            }
        });
        botonesView.findViewById(R.id.pag_anterior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasa_pagina("anterior",true);
            }
        });
        botonesView.findViewById(R.id.boton_ir_a).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SuperAbuela", "startListening");
                speech.startListening(voice);
            }
        });

        //lee configuración
        title_conf = getTitle_conf();
        Log.i("SuperAbuela", "Libro Actual Configurado=" + title_conf);

        //inicializa la Activity de Permisos
        Intent intentup = new Intent(this, RuntimePermissionForUser.class);
        intentup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentup);
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        boolean para_default = false;
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            //viewdDebugText(String.valueOf(event.getKeyCode()));
            //Log.i("SuperAbuela", "Key pressed via accessibility is: " + event.getKeyCode());
            //This allows the key pressed to function normally after it has been used by your app.
            switch (event.getKeyCode()) {
                case 143:
                    //tecla NumLock
                    numlock = (numlock) ? false : true;
                    break;
                case 156:
                    //tecla menos
                    if (!numlock) {
                        inicializa_comando("open-app");
                        process_comando();
                        para_default=true;
                    }
                    break;
                case 155:
                    //tecla *
                    if (!numlock) {
                        if (package_kindle.equals(last_event_package)) {
                            inicializa_comando("saved-preference-title");
                            process_comando();
                            para_default = true;
                        } else {
                            tostar("Tienes que estar leyendo un libro en el Kindle!");
                        }
                    }
                    break;
                case 148:
                    //tecla <-
                    if (!numlock) {
                        pasa_pagina("anterior",true);
                        para_default=true;
                    }
                    break;
                case 150:
                    //tecla ->
                    if (!numlock) {
                        pasa_pagina("siguiente", true);
                        para_default=true;
                    }
                    break;
                case 152:
                    //tecla [flecha arriba]
                    if (!numlock) {
                        setVolumen(true);
                        para_default=true;
                    }
                    break;
                case 146:
                    //tecla [flecha abajo]
                    if (!numlock) {
                        setVolumen(false);
                        para_default=true;
                    }
                    break;
                case 160:
                    //tecla [Enter]
                    Log.i("SuperAbuela", "startListening");
                    speech.startListening(voice);
                    break;
                case 154:
                    //tecla [/]
					if (hay_pantalla) {
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
					}
                    break;
            }
        }
        boolean retorno=true;
        if ((event.getKeyCode()>=143 && event.getKeyCode()<=158) || event.getKeyCode()==67 || event.getKeyCode()==160 )
            retorno=true;
        else
            retorno=super.onKeyEvent(event);
        return retorno;
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
                List<AccessibilityWindowInfo> currentNode=null;
                if (currentNode !=null)
                    if (currentNode.get(0)!=null)
                        nodeInfo=currentNode.get(0).getRoot();
                //Log.i("SuperAbuela", "onAccessibilityEvent=" + event.getEventType() + "Package=" + event.getPackageName()+" isEnabled="+event.isEnabled()+ " Command:"+command+" Esperar:"+esperar);
                /*if (event.getEventType()==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                    Log.i("SuperAbuela", "------Tipo_Cambio=" + event.getContentChangeTypes());
                logViewHierarchy(nodeInfo, 0);
                Log.i("SuperAbuela", "Encontrado Pantalla Menú:" + isPagina("menu",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla Thumbnail:" + isPagina("thumbnail",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla Pop-up Ir a:" + isPagina("popup-ira",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla lectura:" + isPagina("lectura",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla Biblioteca:" + isPagina("biblioteca",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                Log.i("SuperAbuela", "Encontrado Pantalla seekBar:" + isPagina("seekBar",nodeInfo,(String) event.getPackageName(),event.getEventType(),(String) event.getClassName()));*/
                /*Log.i("SuperAbuela", "onAccessibilityEvent=" + event.getEventType() + "Package=" + event.getPackageName()+" isEnabled="+event.isEnabled());
                Log.i("SuperAbuela", "=================================================");
                logViewHierarchy(nodeInfo, 0);*/
                /*Log.i("SuperAbuela", "=================================================");
                logViewHierarchy(nodeInfo, 0);*/
                if (nodeInfo == null) {
                    return;
                } else {
                    /*Log.i("SuperAbuela", "Encontrado Pantalla Menú:" + isPagina("menu",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                    Log.i("SuperAbuela", "Encontrado Pantalla Thumbnail:" + isPagina("thumbnail",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                    Log.i("SuperAbuela", "Encontrado Pantalla Pop-up Ir a:" + isPagina("popup-ira",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));
                    Log.i("SuperAbuela", "Encontrado Pantalla lectura:" + isPagina("lectura",nodeInfo,(String) event.getPackageName(),event.getEventType(),""));*/
                    boolean ispagina_esperando=false;
                    String pantalla_encontrada="";
                    for (int x=0;x<pantalla_esperando.length;x++) {
                        if (isPagina(pantalla_esperando[x],nodeInfo,(String) event.getPackageName(),event.getEventType(),"")) {
                            pantalla_encontrada=pantalla_esperando[x];
                            ispagina_esperando=true;
                            break;
                        }
                    }
                    if ( esperar && ispagina_esperando) {
                        //Log.i("SuperAbuela", "Esperando - y encontrada pantalla:"+pantalla_encontrada);
                        if (active_nodeInfo!=null) active_nodeInfo.recycle();
                        active_nodeInfo = nodeInfo;
                        last_event_type = event.getEventType();
                        last_event_package = (String) event.getPackageName();
                        last_event_class = (String) event.getClassName();
                    }
                }
            if (!esperar) {
                //Log.i("SuperAbuela", "No Esperando:"+(String) event.getPackageName());
                if (active_nodeInfo!=null) active_nodeInfo.recycle();
                active_nodeInfo = nodeInfo;
                last_event_type = event.getEventType();
                last_event_package = (String) event.getPackageName();
                last_event_class = (String) event.getClassName();
                process_comando();
            }
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
                    String palabra_l= palabra.toLowerCase();
                    String numero = palabra.replaceAll("[^\\d]", "");
                    if (!"".equals(numero)) {
                        veces = Integer.parseInt(numero);
                        numero_reconocido=numero;
                    }
                    if ("siguiente".equals(palabra_l)) commando="siguiente";
                    if ("siguientes".equals(palabra_l)) commando="siguiente";
                    if ("adelante".equals(palabra_l)) commando="siguiente";
                    if ("anterior".equals(palabra_l)) commando="anterior";
                    if ("anteriores".equals(palabra_l)) commando="anterior";
                    if ("atras".equals(palabra_l)) commando="anterior";
                    if ("saltar".equals(palabra_l)) commando="goto";
                    if ("salta".equals(palabra_l)) commando="goto";
                }
            }

            if (veces==0) veces=1;
            //for(int i=1;i<=repeticion;i++) {
            if ("siguiente".equals(commando)) pasa_pagina("siguiente",false);
            if ("anterior".equals(commando)) pasa_pagina("anterior",false);
            if ("goto".equals(commando)) {
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
            AccessibilityNodeInfo hijo =nodeInfo.getChild(i);
            logViewHierarchy(hijo, depth+1);
            if (hijo!=null) hijo.recycle();
        }
    }
    public void pasa_pagina(String direccion, boolean forzar_una_vez) {
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point max_pantalla = new Point();
        windowManager.getDefaultDisplay().getSize(max_pantalla);
        Path movimiento = new Path();
        int duration=500;
        switch (direccion) {
            case  "siguiente":
                movimiento.moveTo(max_pantalla.x-100,max_pantalla.y/2);
                movimiento.lineTo(1,max_pantalla.y/2);
                break;
            case  "anterior":
                movimiento.moveTo(100,max_pantalla.y/2);
                movimiento.lineTo(max_pantalla.x-1,max_pantalla.y/2);
                break;
            case "click":
                movimiento.moveTo(max_pantalla.x/2,max_pantalla.y/2);
                duration=10;
        }
        GestureDescription.Builder gesto = new GestureDescription.Builder();
        gesto.addStroke(new GestureDescription.StrokeDescription(movimiento,0,duration));
        if (forzar_una_vez)
            dispatchGesture(gesto.build(),null,null);
        else
            dispatchGesture(gesto.build(),new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    if (veces>=100) veces=1; // se puede reconocer como numero una hora 01:00, está se convierte en 100
                    veces = veces -1;
                    if (veces>0) pasa_pagina(direccion,false);
                    super.onCompleted(gestureDescription);
                }
            },null);
    }
    public boolean isPagina (String pantalla, AccessibilityNodeInfo nodeInfo, String packageName, int eventType, String className) {
        boolean encontrado = false;
        boolean seguir = true;
        if (nodeInfo != null && package_kindle.equals(packageName)) {
            switch (pantalla) {
                case "lectura":
                    if ("com.amazon.krf.platform.KRFView".equals(nodeInfo.getClassName()) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                        encontrado = true;
                    }
                    break;
                case "thumbnail":
                    if ("Abrir menú lateral.".equals(nodeInfo.getContentDescription()) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                        encontrado = true;
                    }
                    break;
                case "menu":
                    if ("Ir a".equals(nodeInfo.getText()) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                        encontrado = true;
                    }
                    break;
                case "popup-ira":
                    if (nodeInfo.getText()!=null)
                        if (nodeInfo.getText().length()>=19) {
                            if ("Introduce la página".equals(nodeInfo.getText().toString().substring(0,19)) && eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
                                encontrado = true;
                        }
                    break;
                case "biblioteca":
                    if (nodeInfo.getText()!=null)
                        //if (nodeInfo.getText().length()==16) {
                            if ("De su biblioteca".equals(nodeInfo.getText()) && (eventType==AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||eventType==AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED))
                                encontrado = true;
                        //}
                    break;
                case "seekBar":
                    seguir=false;
                    if ("android.widget.SeekBar".equals(className)) {
                        encontrado = true;
                    }
                    break;
            }
            if (!encontrado && seguir) {
                for (int i = 0; i < nodeInfo.getChildCount(); ++i) {

                    AccessibilityNodeInfo hijo =nodeInfo.getChild(i);
                    encontrado = isPagina(pantalla, hijo, packageName, eventType, className);
                    if (hijo!=null) hijo.recycle();
                    if (encontrado) break;
                }
            }
        }
        return encontrado;
    }
    public AccessibilityNodeInfo getNodeInfo (String control,boolean objetivo_pacial, AccessibilityNodeInfo nodeInfo) {
        AccessibilityNodeInfo node=null;
        boolean parcial1=objetivo_pacial;
        boolean local_check=false;
        if (nodeInfo != null) {
            switch (control) {
                case "book":
                    String content = (String) nodeInfo.getContentDescription();
                    if (content!=null)
                        if (content.toLowerCase().contains(title_conf.toLowerCase())) {
                            node = nodeInfo;
                        }
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
                case "title":
                    if ("android.widget.TextView".equals(nodeInfo.getClassName()) ) {
                        node = nodeInfo;
                    }
                    break;
                case "numero_pagina":
                    //Log.i("SuperAbuela", "Buscando  numero_pagina");
                    if ("android.support.v7.widget.RecyclerView".equals(nodeInfo.getClassName())) {
                        //Log.i("SuperAbuela", "GetInfo_Node: numero_pagina -> control referencia encontrado");
                        parcial1=true;
                        local_check=true;
                    }
                    if ( objetivo_pacial && "android.widget.TextView".equals(nodeInfo.getClassName()) ) {
                        //Log.i("SuperAbuela", "GetInfo_Node: numero_pagina -> control final encontrado");
                        node = nodeInfo;
                    }
                    break;
                case "biblioteca":
                    if ("Cerrar libro.".equals(nodeInfo.getContentDescription()) ) {
                    node = nodeInfo;
                    }
                    break;
            }
            if (node==null) {
                for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                    AccessibilityNodeInfo hijo =nodeInfo.getChild(i);
                    boolean punto_inicial=("numero_pagina".equals(control) && local_check);
                    if (punto_inicial) {
                        //se encontró el control de referencia en esta iteración
                        //sólo se hace recursivo en el segundo hijo
                        if (i == 1) node = getNodeInfo(control, parcial1, hijo);
                    } else
                        node = getNodeInfo(control, parcial1,hijo);
                    //if (hijo!=null) hijo.recycle();
                    if (node != null) break;
                }
            }
        }
        //if (node!=null)
            //Log.i("SuperAbuela", "getNodeInfo_Locate:"+node.getClassName() + "," + node.isClickable() + "," + node.getViewIdResourceName()+","+node.getContentDescription()+","+node.getText());
        return node;
    }
    public void process_comando() {
        //Log.i("SuperAbuela", "Processing_Command:"+command+" Estado:" + status);
        //Log.i("SuperAbuela", "onAccessibilityEvent=" + last_event_type + "Package=" + last_event_package+" ClassName="+last_event_class);
        //logViewHierarchy(active_nodeInfo, 0);
        switch (command) {
            case "finish-command":
                inicializa_comando("");
                break;
            case "goto":
                switch (status) {
                    case 0:
                        pasa_pagina("click",true);
                        proximo_paso(2000,null,1, new String[]{"thumbnail"});
                        break;
                    case 1:
                        if (process_task("thumbnail",1,"hamburger",null, "CLICK","",true))
                            proximo_paso(1000,null, 2, new String[]{"menu"});
                        break;
                    case 2:
                        if (process_task("menu",1,"ira",null,"CLICK","",true))
                            proximo_paso(1000,null,3,new String[]{"popup-ira"});
                        break;
                    case 3:
                        if (process_task("popup-ira",1,"pagina","edit-pagina","CLICK",numero_reconocido,true))
                            proximo_paso(1000,null,4,new String[]{"thumbnail"});
                        break;
                    case 4:
                        if (process_task("thumbnail",1,"numero_pagina",null, "READ","",true)) {
                            int page_readed = 0;
                            int numero_reconocido_int=new Integer(numero_reconocido);
                            if (text_readed == null || "".equals(text_readed))
                                page_readed = new Integer(numero_reconocido);
                            else
                                page_readed = new Integer(text_readed);
                            if (numero_reconocido_int >page_readed) {
                                pasa_pagina("siguiente", true);
                                proximo_paso(1000,null,4,new String[]{"anyScreen"});
                            } else {
                                //Abierta sobre la página correcta
                                proximo_paso(100, null, 5, new String[]{"anyScreen"});
                                break;
                            }
                        }
                        break;
                    case 5:
                        if (process_task("anyScreen",1,null,null,"CLICK",numero_reconocido,false)) {
                            tostar("Saltar página Finalizado!");
                            proximo_paso(0, "finish-command", -1, new String[]{"anyScreen"});
                        }
                        break;
                }
                break;
            case "open-app":
                switch (status) {
                    case 0:
                        launchApp(package_kindle);
                        proximo_paso(2000,null,1,new String[]{"biblioteca","lectura"});
                        break;
                    case 1:
                        boolean repetir=true;
                        if (isPagina("biblioteca",active_nodeInfo,last_event_package,last_event_type,last_event_class)){
                            proximo_paso(0,"open-book", 0, null);
                            repetir=false;
                        }
                        if (isPagina("lectura",active_nodeInfo,last_event_package,last_event_type,last_event_class)){
                            proximo_paso(0,"check-book", 0, null);
                            repetir=false;
                        }
                        reintentos = reintentos +1;
                        if (repetir && reintentos<8) {
                            proximo_paso(100,null, 0, new String[]{"anyScreen"});
                        }
                        else
                            if (repetir && reintentos>=8) {
                                //despues de n-veces no estamos en la pantalla esperada -> paramos el comando
                                tostar("No se ha conseguido Abrir: " + title_conf + "!!");
                                proximo_paso(0, "finish-command", -1, new String[]{"anyScreen"});
                            }
                            if (!repetir)
                                Log.i("SuperAbuela", "Encontrado pantalla en " + reintentos + " veces");
                        break;
                }
                break;
            case "open-book":
                switch (status) {
                    case 0:
                        if ( process_task("biblioteca",1,"book",null,"CLICK","",true)) {
                            proximo_paso(0, "finish-command", -1, new String[]{"anyScreen"});
                            tostar("Forzado Libro: " + title_conf + " abierto!");
                        }
                        break;
                }
                break;
            case "check-book":
                switch (status) {
                    case 0:
                        pasa_pagina("click",true);
                        proximo_paso(2000,null,1, new String[]{"thumbnail"});
                        break;
                    case 1:
                        if (process_task("thumbnail",1,"title",null, "READ","",true))
                            if (title_conf.equals(text_readed)) {
                                pasa_pagina("click",true);
                                tostar("Abierto Libro: " + title_conf );
                                proximo_paso(0,"finish-command", -1, new String[]{"anyScreen"});
                            }
                            else {
                                //Abre Biblioteca
                                if (process_task("thumbnail",1,"biblioteca",null,"CLICK","",true))
                                    proximo_paso(5000,null,2,new String[]{"biblioteca"});
                                break;
                            }
                        break;
                    case 2:
                        //Abierta Biblioteca
                        if (isPagina("biblioteca",active_nodeInfo,last_event_package,last_event_type,last_event_class))
                            proximo_paso(0,"open-book", 0, null);
                        break;
                }
                break;
            case "saved-preference-title":
                switch (status) {
                    case 0:
                        if (isPagina("lectura", active_nodeInfo, last_event_package, last_event_type, last_event_class)) {
                            pasa_pagina("click", true);
                            proximo_paso(2000, null, 1, new String[]{"thumbnail"});
                        } else {
                            tostar("Tienes que estar leyendo un libro!");
                            proximo_paso(0,"finish-command", -1, new String[]{"anyScreen"});
                        }
                        break;
                    case 1:
                        if (process_task("thumbnail",1,"title",null, "READ","",true))
                            if (text_readed !=null) {
                                title_conf= text_readed;
                                Log.i("SuperAbuela", "Libro Actual Configurado=" + title_conf);
                                setTitle_conf(text_readed);
                                tostar("Libro actual configurado.");
                                pasa_pagina("click", true);
                                proximo_paso(0,"finish-command", -1, new String[]{"anyScreen"});
                            }
                        break;
                }
                break;
        }
    }
    public void proximo_paso(int milisegundos_espera,String proximo_command, int proximo_status,String[] proxima_pantalla) {
        if (proximo_command!=null) {
            inicializa_comando(proximo_command);
            process_comando();
        }
        else {
            if (milisegundos_espera > 0) {
                delay(milisegundos_espera);
                esperar = true;
                pantalla_esperando = proxima_pantalla;
            } else {
                esperar = false;
            }
            status = proximo_status;
            contador_paginas = 0;
        }
    }
    public boolean process_task(String pantalla,int veces_sale_pantalla,String control,String control2,String task,String numero, boolean condicion_positiva) {
        boolean encontrado= false;
        if (isPagina(pantalla,active_nodeInfo,last_event_package,last_event_type,last_event_class)) {
            //Log.i("SuperAbuela", "Pantalla SÍ detectada:" + pantalla);
            contador_paginas = contador_paginas +1;
            if (contador_paginas==veces_sale_pantalla) {
                encontrado=true;
                contador_paginas=0;
                if (control!=null) {
                    AccessibilityNodeInfo nodeInfo_control = getNodeInfo(control,false,active_nodeInfo);
                    if (nodeInfo_control!=null) {
                        if (control2!=null) {
                            AccessibilityNodeInfo nodeInfo_control2 = getNodeInfo(control2,false,active_nodeInfo);
                            if (nodeInfo_control2!=null) {
                                Bundle arguments = new Bundle();
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                        numero);
                                nodeInfo_control2.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            }
                        }
                        switch (task) {
                            case "CLICK":
                                if (condicion_positiva) {
                                    if ("book".equals(control)) {
                                        nodeInfo_control.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);;
                                    } else
                                        nodeInfo_control.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                                break;
                            case "READ":
                                text_readed =(String) nodeInfo_control.getText();
                                break;
                        }
                    }
                }
            }
        } else {
            //Log.i("SuperAbuela", "Pantalla no detectada:" + pantalla);
            if (!condicion_positiva) {
                encontrado=true;
                contador_paginas = 0;
                pasa_pagina("click",true);
                Log.i("SuperAbuela", "Tap");
            }
        }
        return encontrado;
    }
    public void delay (int ms) {
        handler.postDelayed(new Runnable() {
            public void run() {
                process_comando();
            }
        }, ms);
    }
    public void launchApp(String package_name) {
        //Log.i("SuperAbuela", "Arrancando aplicación:" + package_name);
        Intent intent = getPackageManager().getLaunchIntentForPackage(package_name);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    public void inicializa_comando(String comando){
        command=comando;
        status=0;
        esperar=false;
        reintentos=0;
        contador_paginas=0;
    }
    public void viewdDebugText(String text) {
        TextView myVolumeTextView = botonesView.findViewById(R.id.debug_text);
        String fg = (String) myVolumeTextView.getText();
        myVolumeTextView.setText(fg+":" + text);
    }
    public String getTitle_conf() {
        String conf="";
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        conf = sharedPref.getString(getString(R.string.saved_kindle_title), "");
        return conf;
    }
    public void setTitle_conf(String conf) {
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_kindle_title), conf);
        editor.commit();
    }
    public void tostar(String mensaje) {
        Toast toast = Toast.makeText(this, mensaje, duration);
        toast.show();
    }
    public void setVolumen(boolean subir) {
        //Primero obtenemos el valor del volumen del telefono
        //getStreamVolume(int streamType)
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        //int volumen = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int volumen_inicial =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int volumen_siguiente = volumen_inicial;
        if (subir)
            volumen_siguiente = volumen_siguiente+(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10);
        else
            volumen_siguiente = volumen_siguiente-(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/10);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,volumen_siguiente ,0);
    }

}


