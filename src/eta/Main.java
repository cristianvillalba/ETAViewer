package eta;

import com.jme3.app.LegacyApplication;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.style.BaseStyles;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Main Class
 * 
 * e to the PI i = (1 + piI/n) n ....esto es un nro complejo...ver video de mathologer para entender por que es una espiral
 * 
 * de la forma modular de los nros complejos se puede entender que es un nro real y una rotacion en el unit circle
 * ver 3blue1brown
 * 
 * 
 * --- angulos entre secuencias:
 * Calc angle distance of point: 1
Angle time: 18.090582
Calc angle distance of point: 2
Angle time: 30.939487
Calc angle distance of point: 3
Angle time: 43.49505
Calc angle distance of point: 4
Angle time: 56.36756
Calc angle distance of point: 5
Angle time: 68.91882
Calc angle distance of point: 6
Angle time: 163.03911 ---ERROR
Calc angle distance of point: 7

Difference between angles
12.848905
12.555563
12.87251
12.55126

		mult	sum
18.090582		1.710253822	12.848905
30.939487		1.405810316	12.555563
43.49505		1.295953448	12.87251
56.36756		1.222668145	12.55126
68.91882		0	-68.91882
 * 
 */
public class Main extends SimpleApplication implements ActionListener, AnalogListener{
    private float realvalue = 0.5f;
    private float imvalue = 0.0f;
    private float imoffset = 0f;
    private Geometry point;
    private Node etanode;
    private Node overlayetanode;
    private Node torsionNode;
    private float time = 0f;
    private float steptime = 0f;
    private int stepcounter = 1;
    private int maxsteps = 150;
    private int globalTorsionIndex = 0;
    float torsionLenght = 0.0f;
    private Container myMainWindow;
    private Container myStepWindow;
    private TextField realField;
    private TextField imgField;
    private TextField imgOffset;
    private TextField anglevector;
    private TextField naturalField;
    private TextField exponentialField;
    private TextField resultComplexField;
    private BitmapText hudTextInfo;
    private ListBox valueList;
    private float scale = 100f;
    private boolean isdragging = false;
    private ArrayList<Vector3f> temppoints = new ArrayList<Vector3f>();
    private ArrayList<Float> tempangles = new ArrayList<Float>();
    private ArrayList<Integer> tempanglescounter = new ArrayList<Integer>();
    
    private boolean GUI = true;
    private boolean automove = false;
    private boolean stepbystep = false;
    private boolean pausecalc = false;
    private float prevangle = 0;
    private boolean startcounting = false;
    private boolean goingdown = true;
    private int indexangle = 1;
    
    private int nodeindex = 0;//index to move each index
    private int iterationanalyze = 0;
    
    public static Main instance;
    
    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024,768);
        
        app.setSettings(settings);
        app.start();
    }

    //@Override
    public void simpleInitApp() {
        instance = this;
        etanode = new Node();
        etanode.setLocalTranslation(0f, 0f, -50f);
        
        rootNode.attachChild(etanode);
        
        overlayetanode = new Node();
        overlayetanode.setLocalTranslation(0f, 0f, -50f);
        
        rootNode.attachChild(overlayetanode);
        
        torsionNode = new Node();
        torsionNode.setLocalTranslation(0f, 0f, -50f);
        
        rootNode.attachChild(torsionNode);
        
        
        this.flyCam.setMoveSpeed(100f);
        this.cam.setLocation(new Vector3f(50f, 25f, 500f));
        this.cam.lookAt(new Vector3f(25f,0f,0f), Vector3f.UNIT_Y);
        
        // Initialize the globals access so that the default
        // components can find what they need.
        GuiGlobals.initialize(this);
        // Load the 'glass' style
        BaseStyles.loadGlassStyle();
        // Set 'glass' as the default style when not specified
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
        
        RegisterInput();
        
        InitAxis();

        InitPoint();
        
        InitAngles();
        
        if (GUI){
            InitGUI();
        }
    }
    
    private void InitAngles()
    {
        for (int i = 0 ; i < maxsteps; i++)
        {
            tempangles.add(0.0f);
            tempanglescounter.add(0);
        }
    }
    
    private void RegisterInput()
    {
        inputManager.addMapping("Drag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Right", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("Left", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("Up", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("Down", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("Enter", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        
        inputManager.addMapping("arrowup", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("arrowdown", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("arrowleft", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("arrowright", new KeyTrigger(KeyInput.KEY_D));
        
        inputManager.addMapping("writeangles", new KeyTrigger(KeyInput.KEY_0));
        
        inputManager.addListener(this, "Drag");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Enter");
        inputManager.addListener(this, "Pause");
        
        inputManager.addListener(this, "arrowup");
        inputManager.addListener(this, "arrowdown");
        inputManager.addListener(this, "arrowleft");
        inputManager.addListener(this, "arrowright");
        
        inputManager.addListener(this, "writeangles");
    }
    
    private void InitGUI(){
        
        myMainWindow = new Container();
        myStepWindow = new Container();
        realField = new TextField("R:");
        imgField = new TextField("I:");
        anglevector = new TextField("Angle:");
        imgOffset = new TextField("Offset Img:");
        
        realField.setText("" + realvalue);
        imgField.setText("" + imvalue);
        imgOffset.setText("" + imoffset);
        anglevector.setText("" + imoffset);
        
        valueList = new ListBox();
        
        guiNode.attachChild(myMainWindow);

        myMainWindow.setLocalTranslation(10, cam.getHeight()- 10, 0);

        myMainWindow.addChild(new Label("ETA Viewer"));
        myMainWindow.addChild(realField);
        myMainWindow.addChild(imgField);
        myMainWindow.addChild(anglevector);
        myMainWindow.addChild(valueList);
        Button clickMe = myMainWindow.addChild(new Button("Calc ETA..."));
        clickMe.addClickCommands(new Command<Button>() {
                @Override
                public void execute( Button source ) {
                    instance.CalcETA(true, maxsteps, true);
                }
            });
       
        myMainWindow.addChild(imgOffset);
        Button clickMe01 = myMainWindow.addChild(new Button("Offset IMG..."));
        clickMe01.addClickCommands(new Command<Button>() {
                @Override
                public void execute( Button source ) {
                    instance.Offset();
                }
            });
        
        Button clickMe02 = myMainWindow.addChild(new Button("Automove..."));
        clickMe02.addClickCommands(new Command<Button>() {
                @Override
                public void execute( Button source ) {
                    if (automove)
                    {
                        automove = false;
                    }
                    else
                    {
                        automove = true;
                        globalTorsionIndex = 0;
                        torsionLenght = 0;
                    }
                }
            });
        
        Button clickMe03 = myMainWindow.addChild(new Button("Analyze ETA to file..."));
        clickMe03.addClickCommands(new Command<Button>() {
                @Override
                public void execute( Button source ) {
                    instance.CalcEtaToAnalyze(true, maxsteps);
                }
            });
        
        
        naturalField = new TextField("      2       ");
        exponentialField = new TextField("         (2 + 3i)");
        resultComplexField = new TextField("Result: ");
        myStepWindow.setLocalTranslation(cam.getWidth() - 150, cam.getHeight()- 10, 0);
        myStepWindow.addChild(new Label("                  1            "));
        myStepWindow.addChild(new Label("         --------------------  "));
        myStepWindow.addChild(exponentialField);
        myStepWindow.addChild(naturalField);
        myStepWindow.addChild(resultComplexField);
        
        hudTextInfo = new BitmapText(guiFont, false);
        hudTextInfo.setSize(guiFont.getCharSet().getRenderedSize());
        hudTextInfo.setColor(ColorRGBA.White);
        hudTextInfo.setLocalTranslation(200, hudTextInfo.getLineHeight(), 0);
        hudTextInfo.setText("angle: ");
        guiNode.attachChild(hudTextInfo); 
    }
    
    public void CalcETA(boolean focus, int limit, boolean writetofile)
    {
        try{
            if (GUI){
                realvalue = Float.parseFloat(realField.getText());
                imvalue = Float.parseFloat(imgField.getText());
                //point.setLocalTranslation(realvalue * scale, (imvalue - imoffset)*scale, 0f);
            }
            
            ConstructETA(limit, writetofile, true);
            
            if (GUI && focus){
                GuiGlobals.getInstance().releaseFocus(myMainWindow);
            }
        }
        catch(Exception n)
        {
            
        }   
    }
    
    public void CalcEtaToAnalyze(boolean focus, int limit)
    {
        try{

            int numberofpoints = 10;
            
            imvalue = 230.0f;
            
            for (int i = 0; i < numberofpoints; i++)
            {
                realvalue = 0.5f;
                imvalue += 1.0f;
                 
                point.setLocalTranslation(realvalue * scale, (imvalue - imoffset)*scale, 0f);
                ConstructETAToAnalyze(limit, imvalue);
                
            }
            
            
            if (GUI && focus){
                GuiGlobals.getInstance().releaseFocus(myMainWindow);
            }
        }
        catch(Exception n)
        {
            
        }
        
    }
    
    public void Offset()
    {
        try{
            imoffset = Float.parseFloat(imgOffset.getText());
            
            rootNode.detachAllChildren();
            
            etanode = new Node();
            etanode.setLocalTranslation(0f, 0f, -50f);

            rootNode.attachChild(etanode);
            
            InitAxis();
            
            InitPoint();
            
            GuiGlobals.getInstance().releaseFocus(myMainWindow);
        }
        catch(Exception n)
        {
            
        }
        
    }
    
    private void CreateFile(String name)
    {
        try {
            File myObj = new File(name);
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              System.out.println("File already exists.");
            }
          } 
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    
    private void WriteToFile(String name, String data){
        try {
            FileWriter myWriter = new FileWriter(name, true);
            myWriter.write(data);
            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
    }
    
    private void ConstructETA(int limit, boolean writetofile, boolean writeangles)
    {
        temppoints.clear();
        temppoints.add(new Vector3f());
        
        etanode.detachAllChildren();
        torsionNode.detachAllChildren();
        
        Complex etasum = new Complex(0f, 0f);
        Complex one = new Complex(1f, 0f);
        Complex expon = new Complex(realvalue, imvalue);
        Complex segmentdata = new Complex(0f, 0f);
        Complex segmentdataprev = null;
        Complex analyzeorigin = null;
        Complex etasumprev = null;
        Complex anglecalculationV1 = null;
        boolean writeanalyze = false;
        
        etanode.attachChild(GeneratePoint(0f, 0f, 0));
        
        float previousreal = 0f;
        float previousimg = 0f;
        
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(5);
     
        
        String randfilename = "example-" + FastMath.rand.nextInt() + ".csv";
        
        if (writetofile){
            this.CreateFile(randfilename);
        }
        
        //------------
        float torsionIndex = 3.1214256838f * globalTorsionIndex + 7.8518771786f;
        float torsionIndexNext = 3.1214256838f * (globalTorsionIndex + 1) + 7.8518771786f;
      
        boolean torsionDetected = false;
        hudTextInfo.setText("torsionIndex: " + torsionIndex + " torsionNext: " + torsionIndexNext);
        //------------
        
        for (int i = 1; i < limit; i++)
        {
            Complex divisor = new Complex(i, 0f);
            
            segmentdata = one.divide(divisor.pow(expon));//save segment data to visualize
            
            etasumprev = etasum; //store previous eta value
            
            if ( i % 2 == 1){
                etasum = etasum.add(one.divide(divisor.pow(expon)));
            }
            else
            {
                etasum = etasum.subtract(one.divide(divisor.pow(expon)));
                segmentdata = segmentdata.multiply(-1.0f);//correct sign of individual term
            }
            
            if (imvalue > torsionIndex && imvalue < torsionIndexNext  && (globalTorsionIndex + 3) == i )
            {
                float segmentTorsionScale = (imvalue - torsionIndex )/ (torsionIndexNext - torsionIndex);
                Complex segmentTorsion = etasumprev.add(segmentdata.multiply(segmentTorsionScale));
                
                ConstructTorsion((float)segmentTorsion.getReal(), (float)segmentTorsion.getImaginary());
                
                if (Math.abs(imvalue - torsionIndexNext) < 0.1 && !torsionDetected)
                {
                    globalTorsionIndex = globalTorsionIndex + 1;
                    torsionDetected = true; //prevent index to blow
                }
             
            }
            
            if (writetofile){
                this.WriteToFile(randfilename, i + ", " + segmentdata.getReal() + ", " + segmentdata.getImaginary() + ", " + etasum.getReal() + ", " + etasum.getImaginary() + ", " + (segmentdata.getArgument() + Math.PI*2)+ ", " + Math.toDegrees((segmentdata.getArgument() + Math.PI*2)) + "\n");
            }
            
            if (segmentdataprev != null)
            {
                if (angleIsZero(segmentdataprev.getReal(), segmentdataprev.getImaginary(), 
                                segmentdata.getReal(), segmentdata.getImaginary()))
                {
                    System.out.println("Angle 0 in iteration: " + i);
                    System.out.println("Im number: " + imvalue);
                    
                    if (i > iterationanalyze && !writeanalyze)
                    {
                        iterationanalyze = i;
                        writeanalyze = true;
                        analyzeorigin = etasumprev;
                        anglecalculationV1 = segmentdata.subtract(segmentdataprev);
                    }
                }
            }
            
            etanode.attachChild(GeneratePoint( (float)etasum.getReal()*scale, (float)etasum.getImaginary()*scale,i));
            etanode.attachChild(GenerateLine(previousreal, previousimg,(float)etasum.getReal(), (float)etasum.getImaginary(),i ));
            
            //calculate how many full turns
            if (writeangles)
            {
                float angle = (float) segmentdata.getArgument();
                
                if ((i % 2) == 0)
                {
                    angle = (angle - FastMath.PI) * -1.0f; 
                }
                else
                {
                    if (angle < 0)
                        angle *= -1.0f;
                    else
                        angle = FastMath.PI + (FastMath.PI - angle);
                }

                if (FastMath.abs(angle - tempangles.get(i)) > (FastMath.TWO_PI * 0.95f)) //detects a complete circle rotation
                {
                    tempanglescounter.set(i, tempanglescounter.get(i) + 1);
                    //System.out.println("Count 360!");
                }
                
                if (i == 4) //it starts with 2 as the first segment
                {
                    //hudTextInfo.setText("angle: " + angle);
                }
                tempangles.set(i, angle);  
            }
            //calculate how many full turns
            
            previousreal =  (float)etasum.getReal();
            previousimg = (float)etasum.getImaginary();
            
            
            segmentdataprev = segmentdata;
            
            temppoints.add(new Vector3f(previousreal, previousimg, 0.0f));
        }
        
        //etanode.attachChild(GenerateLine(realvalue, imvalue - imoffset, previousreal, previousimg, 666)); //remove red line
        resultComplexField.setText("Result: " + df.format(segmentdata.getReal()) + " " + df.format(segmentdata.getImaginary()) + "i");
        
        if (writeangles && FastMath.abs((float)etasum.getReal()) < 0.1f && FastMath.abs((float)etasum.getImaginary()) < 0.1f)
        {
            WriteAngles(randfilename);
        }
        
        if (writeanalyze)
        {
            Complex anglecalculationV2 = etasum.subtract(analyzeorigin);
            float angle = angleBetweenComplex((float)anglecalculationV1.getReal(), (float)anglecalculationV1.getImaginary(),(float) anglecalculationV2.getReal(), (float)anglecalculationV2.getImaginary());
            
            System.out.println("iteration: " + iterationanalyze + " real: " + df.format(etasum.getReal() - analyzeorigin.getReal()) + " img: " + df.format(etasum.getImaginary()- analyzeorigin.getImaginary()));
            System.out.println("Angle - - - : " + angle);
            
            overlayetanode.attachChild(GenerateLine((float)analyzeorigin.getReal(), (float)analyzeorigin.getImaginary(),(float)etasum.getReal(), (float)etasum.getImaginary(),666 ));
        }
    }
    
    private void ConstructETAToAnalyze(int limit, float name)
    {
        temppoints.clear();
        temppoints.add(new Vector3f());
        
        etanode.detachAllChildren();
        
        Complex etasum = new Complex(0f, 0f);
        Complex one = new Complex(1f, 0f);
        Complex expon = new Complex(realvalue, imvalue);
        Complex segmentdata = new Complex(0f, 0f);
        
        etanode.attachChild(GeneratePoint(0f, 0f, 0));
        
        float previousreal = 0f;
        float previousimg = 0f;
        
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(5);
     
        
        String randfilename = "example-" + name + ".csv";
        
        float[] realsorted = new float[limit];
        float[] imagsorted = new float[limit];

        ArrayList<String> fileoutput = new ArrayList<String>();
 
        this.CreateFile(randfilename);
        
        for (int i = 1; i < limit; i++)
        {
            Complex divisor = new Complex(i, 0f);
            
            segmentdata = one.divide(divisor.pow(expon));//save segment data to visualize
            
            if ( i % 2 == 1){
                etasum = etasum.add(one.divide(divisor.pow(expon)));
            }
            else
            {
                etasum = etasum.subtract(one.divide(divisor.pow(expon)));
                segmentdata = segmentdata.multiply(-1.0f);//correct sign of individual term
            }
            
            
            fileoutput.add(i + ", " + segmentdata.getReal() + ", " + segmentdata.getImaginary() + ", " + etasum.getReal() + ", " + etasum.getImaginary() + ", ");
            
            realsorted[i - 1] = (float)segmentdata.getReal();
            imagsorted[i - 1] = (float)segmentdata.getImaginary();
            
            etanode.attachChild(GeneratePoint( (float)etasum.getReal()*scale, (float)etasum.getImaginary()*scale,i));
            etanode.attachChild(GenerateLine(previousreal, previousimg,(float)etasum.getReal(), (float)etasum.getImaginary(),i ));
            
            previousreal =  (float)etasum.getReal();
            previousimg = (float)etasum.getImaginary();
            
            temppoints.add(new Vector3f(previousreal, previousimg, 0.0f));
        }
        
        Arrays.sort(realsorted);
        Arrays.sort(imagsorted);

        for (int i = 1; i < limit; i++)
        {
            this.WriteToFile(randfilename, fileoutput.get(i - 1) + realsorted[i - 1] +", " + imagsorted[i - 1] + "\n");
        }
        
        resultComplexField.setText("Result: " + df.format(segmentdata.getReal()) + " " + df.format(segmentdata.getImaginary()) + "i");
    }
    
    
    
    private Geometry GeneratePoint(float r, float i, int index)
    {
        Sphere svalue = new Sphere(4,4, 0.5f);
        
        Geometry point1 = new Geometry("svalue" + index, svalue);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Blue);
        point1.setMaterial(mat1);
        
        point1.setLocalTranslation(r, i, 0f);
        return point1;
    }
    
    private Geometry GenerateLine(float r0, float i0, float r1, float i1, int index)
    {
        Vector3f origin = new Vector3f(r0*scale, i0*scale, 0f);
        Vector3f destination = new Vector3f(r1*scale, i1*scale, 0f);
        
        Line linexp = new Line(origin, destination);
        
        Geometry linexpos = new Geometry("etaline" + index, linexp);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        
        if (index == 666){
            mat1.setColor("Color", ColorRGBA.Red);
        }
        else if (index == 667)
        {
            mat1.setColor("Color", ColorRGBA.Orange);
        }
        else
        {
            mat1.setColor("Color", ColorRGBA.Green);
        }
        linexpos.setMaterial(mat1);
        
        return linexpos;
    }
    
    private void InitAxis()
    {
        Line linexp = new Line(Vector3f.ZERO, Vector3f.UNIT_X.mult(500));
        
        Geometry linexpos = new Geometry("xaxispos", linexp);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.White);
        linexpos.setMaterial(mat1);
        
        Line linexn = new Line(Vector3f.ZERO, Vector3f.UNIT_X.mult(-500));
        
        Geometry linexneg = new Geometry("xaxisneg", linexn);
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.White);
        linexneg.setMaterial(mat2);
        
        Line lineyp = new Line(Vector3f.ZERO, Vector3f.UNIT_Y.mult(500));
        
        Geometry lineypos = new Geometry("yaxispos", lineyp);
        Material mat3 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat3.setColor("Color", ColorRGBA.White);
        lineypos.setMaterial(mat3);
        
        Line lineyn = new Line(Vector3f.ZERO, Vector3f.UNIT_Y.mult(-500));
        
        Geometry lineyneg = new Geometry("yaxisneg", lineyn);
        Material mat4 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat4.setColor("Color", ColorRGBA.White);
        lineyneg.setMaterial(mat4);
        
        Line linecritic = new Line(new Vector3f(0.5f*scale, -500, 0), new Vector3f(0.5f*scale, 500, 0));
        
        Geometry linecrytic = new Geometry("critic", linecritic);
        Material mat4a = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat4a.setColor("Color", ColorRGBA.Yellow);
        linecrytic.setMaterial(mat4a);
        
        Line zero01 = new Line(new Vector3f(-500, (14.1347f - imoffset)*scale, 0), new Vector3f(500, (14.1347f - imoffset)*scale, 0));
        
        Geometry zero01g = new Geometry("zero1", zero01);
        Material matz1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matz1.setColor("Color", ColorRGBA.Green);
        zero01g.setMaterial(matz1);
        
        Line zero02 = new Line(new Vector3f(-500, (21.02203f - imoffset)*scale, 0), new Vector3f(500, (21.02203f - imoffset)*scale, 0));
        
        Geometry zero02g = new Geometry("zero2", zero02);
        Material matz2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matz2.setColor("Color", ColorRGBA.Green);
        zero02g.setMaterial(matz2);
        
        Node realaxis = new Node();
        
        realaxis.attachChild(linexpos);
        realaxis.attachChild(linexneg);
        realaxis.attachChild(lineypos);
        realaxis.attachChild(lineyneg);
        realaxis.attachChild(linecrytic);
        realaxis.attachChild(zero01g);
        realaxis.attachChild(zero02g);
        
        Material gray = mat2.clone();
        gray.setColor("Color", ColorRGBA.Gray);
        
        Geometry linexpos2 = linexpos.clone();
        linexpos2.setMaterial(gray);
        Geometry linexneg2 = linexneg.clone();
        linexneg2.setMaterial(gray);
        Geometry lineypos2 = lineypos.clone();
        lineypos2.setMaterial(gray);
        Geometry lineyneg2 = lineyneg.clone();
        lineyneg2.setMaterial(gray);
        
        Node realaxis2 = new Node();
        
        realaxis2.attachChild(linexpos2);
        realaxis2.attachChild(linexneg2);
        realaxis2.attachChild(lineypos2);
        realaxis2.attachChild(lineyneg2);
        
        realaxis2.setLocalTranslation(0, 0, -50f);
        
        rootNode.attachChild(realaxis);
        rootNode.attachChild(realaxis2);
        
    }
    
    private void InitPoint()
    {
        Sphere svalue = new Sphere(4,4, 2.5f);
        
        point = new Geometry("svalue", svalue);
        Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat1.setColor("Color", ColorRGBA.Blue);
        point.setMaterial(mat1);
        
        point.setLocalTranslation(realvalue * scale, (imvalue - imoffset)* scale, 0f);
        rootNode.attachChild(point);
        
    }
    
    private void HitTest()
    {
        Vector2f mouseCoords = new Vector2f(inputManager.getCursorPosition());
        Ray mouseRay = new Ray(cam.getWorldCoordinates(mouseCoords, 0),cam.getWorldCoordinates(mouseCoords, 1).subtractLocal(cam.getWorldCoordinates(mouseCoords, 0)).normalizeLocal());
          
        CollisionResults results = new CollisionResults();

        point.collideWith(mouseRay, results);

        if (results.size() > 0)
        {
            this.isdragging = true;
        }  
    }
    
    private void MovePoint()
    {
        Vector2f mouseCoords = new Vector2f(inputManager.getCursorPosition());
        Ray mouseRay = new Ray(cam.getWorldCoordinates(mouseCoords, 0),cam.getWorldCoordinates(mouseCoords, 1).subtractLocal(cam.getWorldCoordinates(mouseCoords, 0)).normalizeLocal());
          
        Vector3f newpos = new Vector3f();
        Plane plane = new Plane(Vector3f.UNIT_Z,0f);
        
        mouseRay.intersectsWherePlane(plane, newpos);
        
        realvalue = newpos.x / scale;
        imvalue = newpos.y / scale + imoffset;
        
        realField.setText("" + realvalue);
        imgField.setText("" + imvalue);
        
        CalcETA(false, maxsteps, false);
        
        point.setLocalTranslation(newpos.x, newpos.y, 0f);
    }
    
    private void MoveAuto(float tpf, boolean stepbystep)
    {
        if (stepbystep){
            CalcETA(false, stepcounter, false);
        }
        else{
            realvalue = 0.5f;
            imvalue = imvalue + 0.5f*tpf;

            if (GUI){
                realField.setText("" + realvalue);
                imgField.setText("" + imvalue);
            }
        
            CalcETA(false, maxsteps, false);
        }
    }
    
    private void CheckAngles()
    {
        if (temppoints.size() < 3)
        {
            return;
        }
        
        Vector3f previouspoint = temppoints.get(indexangle - 1).clone();
        Vector3f actualpoint = temppoints.get(indexangle).clone();
        Vector3f nextpoint = temppoints.get(indexangle + 1).clone();
        
        Vector3f ppreviouspoint = previouspoint.subtract(actualpoint);
        Vector3f pnextpoint = nextpoint.subtract(actualpoint);
        
        ppreviouspoint.normalizeLocal();
        pnextpoint.normalizeLocal();
        
        float angle = ppreviouspoint.angleBetween(pnextpoint);
        
        if (FastMath.abs((float) (angle - Math.PI)) < 0.01f && !goingdown)
        {
            goingdown = true;
        }
        
        if (prevangle < angle && prevangle < 0.1f && angle < 0.1f && goingdown)
        {
            if (!startcounting){
                time = 0;
                startcounting = true;
                goingdown = false;
                System.out.println("Calc angle distance of point: " + indexangle);
            }
            else
            {
                if (GUI){
                    valueList.getModel().add("Angle time:" + Float.toString(time));
                }
                else
                {
                    System.out.println("Angle time: " + Float.toString(time));
                }
                
                startcounting = false;
                
                if (indexangle < temppoints.size() - 1)
                {
                    indexangle++;
                }
            }
            
        }
        
        anglevector.setText(Float.toString(angle));
        prevangle = angle;
    }

    @Override
    public void simpleUpdate(float tpf) {
        time += tpf;
        
        if (!pausecalc){
            steptime += tpf;
        }
         
        if (automove){
            MoveAuto(tpf, false);
            CheckAngles(); 
        }
        
        if (stepbystep)
        {
            if (steptime > 1.5f)
            {
                steptime = 0;
                stepcounter++;
                //System.out.println("Tick step!");
                
                if (stepcounter > maxsteps )
                {
                    stepcounter = 1;
                }
                
                naturalField.setText("      "+ (stepcounter - 1)+"       ");
            }
            MoveAuto(tpf, true);
            //System.out.println("Tick!");
        }
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("Drag"))
        {
            if (isPressed)
            {
                HitTest();
            }
            else
            {
                this.isdragging = false;
            }
        }
        
        if (name.equals("Pause") && isPressed)
        {
            this.pausecalc = !this.pausecalc;
        }
        
        if (name.equals("Enter") && isPressed)
        {
            this.stepbystep = !this.stepbystep;
            
            if (this.stepbystep)
            {
                guiNode.attachChild(myStepWindow);
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                
                steptime = 0f;
                stepcounter = 1;
                
                naturalField.setText("      "+ (stepcounter - 1)+"       ");
                exponentialField.setText("         (" + decimalFormat.format(realvalue) + " + i" + decimalFormat.format(imvalue) + ")");
            }
            else
            {
                guiNode.detachChild(myStepWindow);
            }
        }
        
        if (name.equals("arrowleft") && isPressed)
        {
            if (nodeindex > 0)
            {
                nodeindex--;
            }
        }
        
        if (name.equals("arrowright") && isPressed)
        {
            nodeindex++;
        }
        
        if (name.equals("writeangles") && isPressed)
        {
            WriteAngles("nafta.csv");
        }

    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (this.isdragging){
            if (name.equals("Right") || name.equals("Left") || name.equals("Up") || name.equals("Down"))
            {
                MovePoint();
            }
        }
        
        if (name.equals("arrowup"))
        {
            MoveSegment(0);
        }
        
        if (name.equals("arrowdown"))
        {
            MoveSegment(1);
        }
        
    }
    
    private void MoveSegment(int direction)
    {
        if (direction == 0)
        {
            etanode.rotate(0, 0, 0.01f);
        }
        else{
            etanode.rotate(0, 0, -0.01f);
        }
    }
    
    private void WriteAngles(String filename)
    {
         this.WriteToFile(filename, "i: " + imvalue + "\n");
         
         StringBuilder sbangles = new StringBuilder();
         StringBuilder sbcounter = new StringBuilder();
         
         for (int i = 0; i < tempangles.size(); i++)
         {
             sbangles.append(tempangles.get(i).toString() + ",");
             Integer counter = new Integer(tempanglescounter.get(i) - 1);
             
             sbcounter.append(counter.toString() + ",");
         }
         
         sbangles.append("\n");
         sbcounter.append("\n");
         
         this.WriteToFile(filename, sbangles.toString());
         this.WriteToFile(filename, sbcounter.toString());
    }
    
    public static boolean angleIsZero(
        double prevDx, double prevDy,
        double currDx, double currDy) 
    {

        // producto cruzado en 2D
        double cross = prevDx * currDy - prevDy * currDx;

        // si el cross no es ≈ 0 → NO son colineales → no puede ser 0°
        if (Math.abs(cross) > 0.001)
            return false;

        // producto punto
        double dot = prevDx * currDx + prevDy * currDy;

        // si el dot > 0 → apuntan al MISMO lado → ángulo 0°
        return dot > 0;
    }
    
    public static float angleBetweenComplex(
        float r1, float i1,
        float r2, float i2)
    {
        float dot = r1 * r2 + i1 * i2;
        float mag1 = FastMath.sqrt(r1 * r1 + i1 * i1);
        float mag2 = FastMath.sqrt(r2 * r2 + i2 * i2);

        if (mag1 == 0 || mag2 == 0) {
            return 0f; // vector degenerado
        }

        float cos = dot / (mag1 * mag2);

        // seguridad numérica
        cos = FastMath.clamp(cos, -1f, 1f);

        return FastMath.acos(cos); // en radianes
    }
    
    private void ConstructTorsion(float r1param, float i1param)
    {
        float r1 = r1param;
        float i1 = i1param;
        
        //float r2 = r1 + 0.72f * (float)Math.cos(5.0f - 0.05f * (imvalue));
        //float i2 = i1 + 0.72f * (float)Math.sin(5.0f - 0.05f * (imvalue));
        
        float exponent = FastMath.pow(imvalue, 1.05f);
        float r2 = r1 + 0.72f * (float)Math.cos(5.0f - 1.39f * (exponent));
        float i2 = i1 + 0.72f * (float)Math.sin(5.0f - 1.39f * (exponent));
        
        
        torsionNode.attachChild(GenerateLine(r1,i1,r2,i2,667));
    }
}
