package eta;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResults;
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
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.style.BaseStyles;
import java.text.DecimalFormat;
import java.util.ArrayList;

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
    private float realvalue = 1.0f;
    private float imvalue = 1.0f;
    private float imoffset = 0f;
    private Geometry point;
    private Node etanode;
    private float time = 0f;
    private float steptime = 0f;
    private int stepcounter = 1;
    private int maxsteps = 150;
    private Container myMainWindow;
    private Container myStepWindow;
    private TextField realField;
    private TextField imgField;
    private TextField imgOffset;
    private TextField anglevector;
    private TextField naturalField;
    private TextField exponentialField;
    private TextField resultComplexField;
    private ListBox valueList;
    private float scale = 100f;
    private boolean isdragging = false;
    private ArrayList<Vector3f> temppoints = new ArrayList<Vector3f>();
    
    private boolean GUI = true;
    private boolean automove = false;
    private boolean stepbystep = false;
    private boolean pausecalc = false;
    private float prevangle = 0;
    private boolean startcounting = false;
    private boolean goingdown = true;
    private int indexangle = 1;
    
    private int nodeindex = 0;//index to move each index
    
    public static Main instance;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        instance = this;
        etanode = new Node();
        etanode.setLocalTranslation(0f, 0f, -50f);
        
        rootNode.attachChild(etanode);
        
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
        
        if (GUI){
            InitGUI();
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
                    instance.CalcETA(true, maxsteps);
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
                    }
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
      
    }
    
    public void CalcETA(boolean focus, int limit)
    {
        try{
            if (GUI){
                realvalue = Float.parseFloat(realField.getText());
                imvalue = Float.parseFloat(imgField.getText());
                point.setLocalTranslation(realvalue * scale, (imvalue - imoffset)*scale, 0f);
            }
            
            ConstructETA(limit);
            
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
    
    private void ConstructETA(int limit)
    {
        temppoints.clear();
        temppoints.add(new Vector3f());
        
        etanode.detachAllChildren();
        
        Complex etasum = new Complex(0f, 0f);
        Complex one = new Complex(1f, 0f);
        Complex expon = new Complex(realvalue, imvalue);
        
        etanode.attachChild(GeneratePoint(0f, 0f, 0));
        
        float previousreal = 0f;
        float previousimg = 0f;
        
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        
        for (int i = 1; i < limit; i++)
        {
            Complex divisor = new Complex(i, 0f);
            
            if ( i % 2 == 1){
                etasum = etasum.add(one.divide(divisor.pow(expon)));
            }
            else
            {
                etasum = etasum.subtract(one.divide(divisor.pow(expon)));
            }
            
            etanode.attachChild(GeneratePoint( (float)etasum.getReal()*scale, (float)etasum.getImaginary()*scale,i));
            etanode.attachChild(GenerateLine(previousreal, previousimg,(float)etasum.getReal(), (float)etasum.getImaginary(),i ));
            
            previousreal =  (float)etasum.getReal();
            previousimg = (float)etasum.getImaginary();
            
            temppoints.add(new Vector3f(previousreal, previousimg, 0.0f));
        }
        
        //etanode.attachChild(GenerateLine(realvalue, imvalue - imoffset, previousreal, previousimg, 666)); //remove red line
        resultComplexField.setText("Result: " + df.format(etasum.getReal()) + " " + df.format(etasum.getImaginary()) + "i");
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
        
        if (index != 666){
            mat1.setColor("Color", ColorRGBA.Green);
        }
        else
        {
            mat1.setColor("Color", ColorRGBA.Red);
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
        
        CalcETA(false, maxsteps);
        
        point.setLocalTranslation(newpos.x, newpos.y, 0f);
    }
    
    private void MoveAuto(float tpf, boolean stbst)
    {
        if (stbst){
            CalcETA(false, stepcounter);
        }
        else{
            realvalue = 0.5f;
            imvalue = imvalue + 0.5f*tpf;

            if (GUI){
                realField.setText("" + realvalue);
                imgField.setText("" + imvalue);
            }
        
            CalcETA(false, maxsteps);
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
}
