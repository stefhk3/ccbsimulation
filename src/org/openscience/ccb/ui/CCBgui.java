package org.openscience.ccb.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.jgrapht.Graph;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.AsUnweightedDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.openscience.ccb.action.Action;
import org.openscience.ccb.action.WeakAction;
import org.openscience.ccb.parser.CCBParser;
import org.openscience.ccb.predicate.Connectivity;
import org.openscience.ccb.predicate.Distance;
import org.openscience.ccb.process.Prefix;
import org.openscience.ccb.process.Process;
import org.openscience.ccb.reduction.CCBVisitor;
import org.openscience.ccb.reduction.Move;
import org.openscience.ccb.reduction.Prom;
import org.openscience.ccb.synchronisation.Synchronize;
import org.openscience.ccb.transition.DummyTransition;
import org.openscience.ccb.transition.Transition;
import org.openscience.ccb.util.CCBConfiguration;
import org.openscience.ccb.util.CCBException;
import org.openscience.ccb.util.GraphChecks;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

public class CCBgui extends Application {
	
    Map<Process,List<Transition>> newProcesses=new HashMap<Process,List<Transition>>();
	ObservableList<Transition> items;
	ListView<Transition> listView;
	Map<Integer,Integer> transitionsmap;
	Canvas detailscanvas;
	Button btnAll;
	CCBConfiguration ccbconfiguration;
    Synchronize synchronize = new Synchronize(new String[]{});
    Canvas canvas;
    int newkey=100;
    ObservableList<Process> previousitems;
	ListView<Process> previous;
    FileChooser fileChooser=new FileChooser();
    Stage stage;
    List<Action> weakActionsList;
    //This holds the processes reached so far and the possible transitions
    AsUnweightedDirectedGraph<Process, DefaultEdge> doneProcesses=new AsUnweightedDirectedGraph<Process, DefaultEdge>(new DefaultDirectedWeightedGraph<Process, DefaultEdge>(DefaultEdge.class));
    int processcounter=0;
    Map<Process,List<Double>> processesmap=new HashMap<Process, List<Double>>();

    @Override
    public void init(){
    	try{
		    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ccbconfiguration_default.properties");
		    Properties ccbprops = new Properties();
		    ccbprops.load(inputStream);
		    ccbconfiguration=new CCBConfiguration(ccbprops);
    	}catch(Exception ex){
			Alert alert=new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText(ex.getMessage());
			alert.showAndWait();
			System.exit(1);
    	}
    }
    
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		SplitPane splitPane=new SplitPane();
		previous=new ListView<Process>();
		previous.setMaxHeight(Double.MAX_VALUE);
		previous.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		previousitems=FXCollections.observableArrayList();
		previous.setItems(previousitems);
		previous.getSelectionModel().selectedItemProperty().addListener(new ProcessSelectionAction());
		previous.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				listView.getSelectionModel().clearSelection();
			}
		});
		VBox vbox=new VBox();
		vbox.setSpacing(10);
		listView=new ListView<Transition>();
		listView.setMaxHeight(Double.MAX_VALUE);
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		items=FXCollections.observableArrayList();
		listView.setItems(items);
		listView.getSelectionModel().selectedItemProperty().addListener(new TransitionSelectionAction());
		listView.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				previous.getSelectionModel().clearSelection();
			}
		});
		CanvasPane detailsCanvasPane=new CanvasPane(700,500);
		detailsCanvasPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			  @Override
	          public void handle(MouseEvent me) {
	        	  for(Process p : processesmap.keySet()){
	        		  double x=me.getX();
	        		  double y=me.getY();
	        		  if(processesmap.get(p).get(0)>x-15 && processesmap.get(p).get(0)<x+15 && processesmap.get(p).get(1)>y-15 && processesmap.get(p).get(1)<y+15){
	        			  try{
		        			  GraphChecks gc=new GraphChecks(p);
		        			  draw(gc.getGraph(), canvas, false);
		        			  break;
	        			  }catch(CCBException ex){
	        					handleException(ex);
	        			  }
	        		  }
	        	  }
	          }
	        });
		Tooltip mousePositionToolTip = new Tooltip("");
		detailsCanvasPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
		        @Override
		        public void handle(MouseEvent me) {
		        	mousePositionToolTip.hide();
		        	  for(Process p : processesmap.keySet()){
		        		  double x=me.getX();
		        		  double y=me.getY();
		        		  if(processesmap.get(p).get(0)>x-15 && processesmap.get(p).get(0)<x+15 && processesmap.get(p).get(1)>y-15 && processesmap.get(p).get(1)<y+15){
		      		            mousePositionToolTip.setText(p.toString());
		    		            Node node = (Node) me.getSource();
		    		            mousePositionToolTip.show(node, me.getScreenX() + 50, me.getScreenY());
		    		            break;
		        		  }
		        	  }
		        }
		    });
		detailscanvas=detailsCanvasPane.getCanvas();
		HBox hbox=new HBox();
		hbox.setSpacing(10);
		btnAll=new Button("Evaluate all");
		btnAll.setOnAction(new EvaluateAction());
		Button btnSelected=new Button("Evaluate selected");
		btnSelected.setOnAction(new EvaluateAction());
		Button btnRemove=new Button("Remove process");
		btnRemove.setOnAction(new RemoveAction());
		Button btnBreak=new Button("Break bond");
		btnBreak.setOnAction(new BreakAction());
		hbox.getChildren().addAll(btnAll, btnSelected, btnRemove, btnBreak);
		VBox.setVgrow(listView, Priority.ALWAYS);
		vbox.setPadding(new Insets(10,10,10,10));
		vbox.getChildren().addAll(previous,listView,hbox, detailsCanvasPane);
		CanvasPane canvasPane=new CanvasPane(500,500);
		canvas=canvasPane.getCanvas();
		splitPane.getItems().addAll(vbox, canvasPane);
		BorderPane rootBox=new BorderPane();
		MenuBar menuBar = new MenuBar();
		Menu fileMenu=new Menu();
		fileMenu.setMnemonicParsing(true);
		fileMenu.setText("_File");
		menuBar.getMenus().add(fileMenu);
		MenuItem newItem=new MenuItem();
		newItem.setMnemonicParsing(true);
		newItem.setText("_New...");
		newItem.setOnAction(new NewAction());
		newItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
		fileMenu.getItems().add(newItem);
		MenuItem loadItem=new MenuItem();
		loadItem.setMnemonicParsing(true);
		loadItem.setText("_Open...");
		loadItem.setOnAction(new LoadAction());
		loadItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		fileMenu.getItems().add(loadItem);
		MenuItem saveItem=new MenuItem();
		saveItem.setMnemonicParsing(true);
		saveItem.setText("_Save as...");
		saveItem.setOnAction(new SaveAction());
		saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		fileMenu.getItems().add(saveItem);
		fileMenu.getItems().add(new SeparatorMenuItem());
		MenuItem exit = new MenuItem("_Quit");
		exit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
		exit.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent t) {
		        System.exit(0);
		    }
		});
		fileMenu.getItems().add(exit);
		Menu toolsMenu=new Menu();
		toolsMenu.setMnemonicParsing(true);
		toolsMenu.setText("_Tools");
		menuBar.getMenus().add(toolsMenu);
		MenuItem equiItem=new MenuItem();
		equiItem.setMnemonicParsing(true);
		equiItem.setText("_Check chemical process equivalence");
		equiItem.setOnAction(new EquiAction());
		toolsMenu.getItems().add(equiItem);
		Menu editMenu=new Menu();
		editMenu.setMnemonicParsing(true);
		editMenu.setText("_Edit");
		menuBar.getMenus().add(editMenu);
		MenuItem prefItem=new MenuItem();
		prefItem.setMnemonicParsing(true);
		prefItem.setText("_Preferences...");
		prefItem.setOnAction(new SettingsAction());
		editMenu.getItems().add(prefItem);
		rootBox.setTop(menuBar);
		rootBox.setCenter(splitPane);
		Scene scene=new Scene(rootBox,1200,800);
		rootBox.prefHeightProperty().bind(scene.heightProperty());
		rootBox.prefWidthProperty().bind(scene.widthProperty());
		primaryStage.setScene(scene);
		primaryStage.setTitle("CCB Tool");
		primaryStage.show();
		this.stage=primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}


	private class SaveAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			doSave();
		}
	}
	

	private void doSave() {
		PrintWriter out=null;
		try{
			throw new IOException("lkkjö");
			/*File file=fileChooser.showSaveDialog(stage);
			if(file==null)
				return;
			out=new PrintWriter(file);
			Element root=new Element("ccb");
			Element pc =new Element("processcounter");
			pc.appendChild(Integer.toString(processcounter));
			root.appendChild(pc);
			Element processes=new Element("processes");
			root.appendChild(processes);
			for(Process p : newProcesses.keySet()){
				processes.appendChild(p.getXML());
			}
			root.appendChild(synchronize.getXML());
			Element weakactions=new Element("weakactions");
			for(Action action : weakActionsList){
				Element actionel=new Element("action");
				actionel.appendChild(action.getActionName());
				weakactions.appendChild(actionel);
			}
			root.appendChild(weakactions);
			Document doc=new Document(root);
			out.println(doc.toXML());*/
		}catch(Exception ex){
			handleException(ex);
		}
		finally{
			if(out!=null)
				out.close();
		}
	}

	private class ProcessSelectionAction implements ChangeListener<Process> {
	    @Override
	    public void changed(ObservableValue<? extends Process> observable, Process oldValue, Process newValue) {
	    	if(newValue==null)
	    		return;
			try{
		    	GraphChecks gc=new GraphChecks(newValue);
		    	draw(gc.getGraph(), canvas, false);
			}catch(Exception ex){
				handleException(ex);
			}
	    }
	}

		
	private class TransitionSelectionAction implements ChangeListener<Transition> {

	    @Override
	    public void changed(ObservableValue<? extends Transition> observable, Transition oldValue, Transition newValue) {
	    	if(newValue==null)
	    		return;
			try{
	    		CCBVisitor move = new Move();
	    		CCBVisitor prom = new Prom();
		        Process clonedProcess=newValue.getClone().clone();
		        int newkey2=newkey;
		        List<Transition> trans=clonedProcess.inferTransitions(synchronize, ccbconfiguration, clonedProcess);
		        clonedProcess.executeTransition(trans.get(transitionsmap.get(listView.getSelectionModel().getSelectedIndex())),newkey2,clonedProcess);
                if(ccbconfiguration.forceMove)
                	clonedProcess.accept(move);
                if(ccbconfiguration.forcePromotion)
                	clonedProcess.accept(prom);
		    	GraphChecks gc=new GraphChecks(clonedProcess);
		    	draw(gc.getGraph(), canvas, false);
			}catch(Exception ex){
				handleException(ex);
			}
	    }
	}

	
	private class BreakAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if(previous.getSelectionModel().getSelectedItems().size()==0){
				Alert alert=new Alert(AlertType.WARNING,"You did not select anything. A selection is needed to break a bond.");
				alert.setTitle("No selection");
				alert.setHeaderText(null);
				alert.showAndWait();				
			}
			TextInputDialog tid=new TextInputDialog();
			tid.setTitle("Enter number of bond to break");
			tid.setHeaderText(null);
			tid.setContentText("Break bond:");
			Optional<String> result = null;
			while(result==null || !result.isPresent()){
				try{
					result=tid.showAndWait();
					if(!result.isPresent())
						return;
					int bondnumber=Integer.parseInt(result.get());
		    		CCBVisitor move = new Move();
		    		CCBVisitor prom = new Prom();
					List<Process> processes=previous.getSelectionModel().getSelectedItems();
					newProcesses.clear();
					ObservableList<Process> previousitemsnew=FXCollections.observableArrayList();
					List<Transition> transitions=new ArrayList<Transition>();
					List<Process> checkedProcesses=new ArrayList<Process>();
					for(Process process : processes){
						Process origin=process;
						Process target=process.clone();
						boolean removed=target.removeKey(bondnumber);
						if(removed){
			                target.counter=processcounter;
			                processcounter++;
		                    if(ccbconfiguration.forceMove)
		                    	target.accept(move);
		                    if(ccbconfiguration.forcePromotion)
		                    	target.accept(prom);
						}
						handleProcess(target, previousitemsnew, transitions, origin, checkedProcesses);
					}
					draw(doneProcesses, detailscanvas, true);
					items.clear();
					items.addAll(transitions);
					previousitems.clear();
					previousitems.addAll(previousitemsnew);
				}catch(Exception ex){
					Alert alert=new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText(ex.getMessage());
					alert.showAndWait();
					result=null;
				}
			}
		}
	}
	
	private class LoadAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			try{
				File file=fileChooser.showOpenDialog(stage);
				if(file==null)
					return;
				Builder parser=new Builder();
				Document doc=parser.build(file);
				Element ccb=doc.getRootElement();
				if(!ccb.getLocalName().equals("ccb"))
					throw new Exception("Your document does not start with a ccb node as expected");
				processcounter=Integer.parseInt(ccb.getFirstChildElement("processcounter").getValue());
				Element synchros=ccb.getFirstChildElement("synchronizes");
				Map<Set<String>,String> gamma=new  HashMap<Set<String>,String>();
				for(int i=0;i<synchros.getChildElements("synchronize").size();i++){
					Element synchro=synchros.getChildElements("synchronize").get(i);
					Set<String> firstandsecond=new HashSet<String>();
					firstandsecond.add(synchro.getFirstChildElement("first").getValue());
					firstandsecond.add(synchro.getFirstChildElement("second").getValue());
					gamma.put(firstandsecond, synchro.getFirstChildElement("result").getValue());
				}
			    weakActionsList=new ArrayList<Action>();
				Element weakactions=ccb.getFirstChildElement("weakactions");
				for(int i=0;i<weakactions.getChildElements().size();i++){
					Element weakaction=weakactions.getChildElements().get(i);
					weakActionsList.add(new WeakAction(weakaction.getValue()));
				}
				newProcesses.clear();
				doneProcesses=new AsUnweightedDirectedGraph<Process, DefaultEdge>(new DefaultDirectedWeightedGraph<Process, DefaultEdge>(DefaultEdge.class));
			    synchronize = new Synchronize(gamma);
				List<Transition> transitions=new ArrayList<Transition>();
				previousitems.clear();
				List<Process> checkedProcesses=new ArrayList<Process>();
				for(int i=0;i<ccb.getFirstChildElement("processes").getChildCount();i++){
					Process process=CCBParser.parseProcess(ccb.getFirstChildElement("processes").getChildElements().get(i), gamma, weakActionsList);
					handleProcess(process, previousitems, transitions,null,checkedProcesses);
		    	}
				draw(doneProcesses, detailscanvas, true);
				items.clear();
				items.addAll(transitions);
			}catch(Exception ex){
				handleException(ex);
			}
		}

	}

	
	private void handleProcess(Process process, ObservableList<Process> previousitems, List<Transition> transitions, Process origin, List<Process> checkedProcesses) throws CCBException {
		previousitems.add(process);
	    List<Transition> localTransitions = process.inferTransitions(synchronize, ccbconfiguration, process);
	    List<Transition> newTransitions = new ArrayList<Transition>();
	    transitionsmap=new HashMap<Integer, Integer>();
        doneProcesses.addVertex(process);
    	newTransitions.add(new DummyTransition("From "+process.counter));
        for(int k=0;k<localTransitions.size();k++){
            Process clone=process.clone();
            Transition transition = clone.inferTransitions(synchronize, ccbconfiguration, clone).get(k);
            //we check if the process reached is new (within this set of transitions) using the isomorphism
            boolean existing=false;
            Process clonedProcess=clone.clone();
            clonedProcess.executeTransition(clonedProcess.inferTransitions(synchronize, ccbconfiguration, clonedProcess).get(k), newkey++, clonedProcess);
    		CCBVisitor move = new Move();
    		CCBVisitor prom = new Prom();
            if(ccbconfiguration.forceMove)
            	clonedProcess.accept(move);
            if(ccbconfiguration.forcePromotion)
            	clonedProcess.accept(prom);
            GraphChecks gc=new GraphChecks(clonedProcess);
            for(int l=0;l<checkedProcesses.size();l++){
                if(gc.isIsomorph(checkedProcesses.get(l)))
                    existing=true;
            }
            if(!existing){
                for(Process doneprocess : doneProcesses.vertexSet()){
                    if(gc.isIsomorph(doneprocess)){
                        existing=true;
                		doneProcesses.addEdge(process,doneprocess);
                		break;
                    }
                }
                if(!existing){
	                clonedProcess.counter=processcounter;
	                clone.counter=processcounter;
	                processcounter++;
	                checkedProcesses.add(clonedProcess);
	                transition.setClone(clone);
	                newTransitions.add(transition);
	                transitionsmap.put(newTransitions.size()-1, k);
                }
            }
        }
		newProcesses.put(process,newTransitions);
		transitions.addAll(newTransitions);
        if(origin!=null){
            GraphChecks gc=new GraphChecks(origin);
            for(Process p : doneProcesses.vertexSet()){
            	if(gc.isIsomorph(p)){
            		doneProcesses.addEdge(p,process);
            		break;
            	}
            }
        }
	}

	
	public void handleException(Exception ex) {
		Alert alert=new Alert(AlertType.ERROR,ex.getMessage());
		alert.setTitle("Sorr, we're having problems");
		alert.setHeaderText("An error occured");
		ex.printStackTrace();
		alert.showAndWait();				
	}


	private class RemoveAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if(listView.getSelectionModel().getSelectedItems().size()==0){
				Alert alert=new Alert(AlertType.WARNING,"You did not select anything, we remove nothing!");
				alert.setTitle("No selection");
				alert.setHeaderText(null);
				alert.showAndWait();
			}else {
				items.removeAll(listView.getSelectionModel().getSelectedItems());
			}
		}
	}
	
	
	private class EvaluateAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
	    	try{
	    		CCBVisitor move = new Move();
	    		CCBVisitor prom = new Prom();
				List<Transition> processes=listView.getSelectionModel().getSelectedItems();
				if(event.getSource()==btnAll)
					processes=items;
				else if(listView.getSelectionModel().getSelectedItems().size()==0){
					Alert alert=new Alert(AlertType.WARNING,"You did not select anything. We will execute all possible transitions.");
					alert.setTitle("No selection");
					alert.setHeaderText(null);
					alert.showAndWait();
					processes=items;
				}
				newProcesses.clear();
				List<Transition> transitions=new ArrayList<Transition>();
                previousitems.clear();
        	    List<Process> checkedProcesses=new ArrayList<Process>();
				for(Transition process : processes){
					if(!(process instanceof DummyTransition)){
						Process origin=process.getClone().clone();
						process.getClone().executeTransition(process, newkey++, process.getClone());
	                    if(ccbconfiguration.forceMove)
	                    	process.getClone().accept(move);
	                    if(ccbconfiguration.forcePromotion)
	                    	process.getClone().accept(prom);
	                    GraphChecks gc=new GraphChecks(process.getClone());
	                    Process oldprocess=null;
	                    for(Process p : doneProcesses.vertexSet()){
	                    	if(gc.isIsomorph(p))
	                    		oldprocess=p;
	                    }
	                    if(oldprocess==null){
	                    	handleProcess(process.getClone(), previousitems, transitions,origin, checkedProcesses);
	                    }
					}
				}
            	draw(doneProcesses, detailscanvas, true);
				items.clear();
				items.addAll(transitions);
	    	}catch(Exception ex){
	    		ex.printStackTrace();
				Alert alert=new Alert(AlertType.ERROR);
				alert.setTitle("Error");
				alert.setHeaderText(null);
				alert.setContentText(ex.getMessage());
				alert.showAndWait();
	    	}
		}
	}
	
	private class NewAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			if(previousitems.size()>0) {
				ButtonType btnYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
				ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);
				ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
				Alert alert=new Alert(AlertType.WARNING,"There are current processes. Do you want to save these?",btnYes, btnNo, btnCancel);
				alert.setTitle("Current state not empty");
				alert.setHeaderText(null);
				Optional<ButtonType> result = alert.showAndWait();				
				if(result.get()==btnCancel) {
					return;
				}else if(result.get()==btnYes) {
					doSave();
				}
			}
			TextInputDialog tid=new TextInputDialog();
			tid.setTitle("Enter weak actions (list like n,p)");
			tid.setHeaderText(null);
			tid.setContentText("Enter weak actions:");
			Optional<String> result = null;
		    weakActionsList=new  ArrayList<Action>();
			while(result==null || !result.isPresent()){
				try{
					result=tid.showAndWait();
					if(!result.isPresent())
						return;
				    StringTokenizer st=new StringTokenizer(result.get(),",");
				    while(st.hasMoreTokens()){
				    	weakActionsList.add(new WeakAction(st.nextToken()));
				    }	    
				}catch(Exception ex){
					Alert alert=new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText(ex.getMessage());
					alert.showAndWait();
					result=null;
				}
			}
			tid=new TextInputDialog();
			tid.setTitle("Enter process");
			tid.setHeaderText(null);
			tid.setContentText("Enter the process:");
			result = null;
			Process process = null;
			while(result==null || !result.isPresent()){
				try{
					result=tid.showAndWait();
					if(!result.isPresent())
						return;
					CCBParser ccbparser=new CCBParser();
					process = ccbparser.parseProcess(result.get(), weakActionsList, ccbconfiguration.connectivityMax>-1 ? new Connectivity(ccbconfiguration.connectivityMax) : null, ccbconfiguration.distanceMin>-1 ? new Distance(ccbconfiguration.distanceMax, ccbconfiguration.distanceMin, ccbconfiguration.distanceAnd) : null);
				}catch(Exception ex){
					Alert alert=new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText(ex.getMessage());
					alert.showAndWait();
					result=null;
					ex.printStackTrace();
				}
			}
			tid=new TextInputDialog();
			tid.setTitle("Enter synchronisation function");
			tid.setHeaderText(null);
			tid.setContentText("Enter synchronisation function [format: a,a,b...):");
			result = null;
			while(result==null || !result.isPresent()){
				try{
					result=tid.showAndWait();
					if(!result.isPresent())
						return;
				    String syncro = result.get();
				    StringTokenizer st=new StringTokenizer(syncro,",");
				    List<String> synchronizeList=new  ArrayList<String>();
				    while(st.hasMoreTokens()){
				        synchronizeList.add(st.nextToken());
				    }
				    synchronize = new Synchronize(synchronizeList.toArray(new String[]{}));
					doneProcesses=new AsUnweightedDirectedGraph<Process, DefaultEdge>(new DefaultDirectedWeightedGraph<Process, DefaultEdge>(DefaultEdge.class));
					process.counter=0;
					processcounter=1;
					draw(doneProcesses, detailscanvas, true);
					List<Transition> transitions=new ArrayList<Transition>();
					previousitems.clear();
					items.clear();
					List<Process> checkedProcesses=new ArrayList<Process>();
					handleProcess(process, previousitems, transitions,null,checkedProcesses);
					items.addAll(transitions);
					newProcesses.clear();
					newProcesses.put(process,transitions);
				}catch(Exception ex){
					Alert alert=new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText(ex.getMessage());
					alert.showAndWait();
					result=null;
				}
			}
		}
	}


	private class SettingsAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			PreferencesDialog prefdialog=new PreferencesDialog(ccbconfiguration);
			if(prefdialog.getResult()!=null)
				ccbconfiguration=prefdialog.getResult();
		}
	}
	
	private class EquiAction implements EventHandler<ActionEvent> {

		@Override
		public void handle(ActionEvent event) {
			EquiDialog equidialog=new EquiDialog(null);
			if(equidialog.getResult()!=null){
				try{
					CCBParser ccbparser=new CCBParser();
					Process p1=ccbparser.parseProcess(equidialog.getResult().getKey(), ccbconfiguration.connectivityMax>-1 ? new Connectivity(ccbconfiguration.connectivityMax) : null, ccbconfiguration.distanceMin>-1 ? new Distance(ccbconfiguration.distanceMax, ccbconfiguration.distanceMin, ccbconfiguration.distanceAnd) : null);
					Process p2=ccbparser.parseProcess(equidialog.getResult().getValue(), ccbconfiguration.connectivityMax>-1 ? new Connectivity(ccbconfiguration.connectivityMax) : null, ccbconfiguration.distanceMin>-1 ? new Distance(ccbconfiguration.distanceMax, ccbconfiguration.distanceMin, ccbconfiguration.distanceAnd) : null);
				    GraphChecks gc=new GraphChecks(p1);
				    if(!gc.isIsomorph(p2)){
						Alert alert=new Alert(AlertType.WARNING);
						alert.setTitle("Not equivalent");
						alert.setHeaderText(null);
						alert.setContentText("Processes are not chemically process equivalent!");
						alert.showAndWait();				    	
				    }else{
				    	IsomorphicGraphMapping<Process, DefaultEdge> mapping =  gc.getMapping(p2).next();
		    			StringBuffer transforms=new StringBuffer();
				    	for(Process pinp1 : gc.getGraph().vertexSet()){
				    		Process pinp2=mapping.getVertexCorrespondence(pinp1, true);
				    		if(pinp1 instanceof Prefix && pinp2 instanceof Prefix){
				    			Set<Action> actionsinp1=((Prefix)pinp1).getAllActionsInOne();
				    			Set<Action> actionsinp2=((Prefix)pinp2).getAllActionsInOne();
				    			Iterator<Action> actionsinp2iterator = actionsinp2.iterator();
				    			for(Action actionin1 : actionsinp1){
				    				Action actionin2=actionsinp2iterator.next();
				    				if(actionin1.getKey()!=actionin2.getKey()){
				    					transforms.append("key "+actionin2.getKey()+" becomes "+actionin1.getKey()+"\r\n");
				    				}
				    				if(actionin1.getSubscript()!=0 && actionin1.getSubscript()!=actionin2.getSubscript()){
				    					transforms.append("subscript "+actionin2.getSubscript()+" becomes "+actionin1.getSubscript()+"\r\n");
				    				}
				    			}
				    		}
				    	}
			    		Alert alert = new Alert(AlertType.INFORMATION);
			    		alert.setTitle("Equivalence exists");
			    		alert.setHeaderText("Your first process can be transformed into the second by applying these operations:");
			    		alert.setContentText(transforms.toString());
			    		alert.showAndWait();
				    }
				}catch(Exception ex){
					Alert alert=new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(null);
					alert.setContentText(ex.getMessage());
					alert.showAndWait();
				}
			}
		}
	}

	
	private void draw(Graph<Process, DefaultEdge> g, Canvas canvas, boolean withhead){
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		//we do the layout using mxgraph
		JGraphXAdapter<Process, DefaultEdge> jgxAdapter = new JGraphXAdapter<Process, DefaultEdge>(g);
		mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);
		double x = 20, y = 20;
		for (mxICell cell : jgxAdapter.getVertexToCellMap().values()) {
			jgxAdapter.getModel().setGeometry(cell, new mxGeometry(x, y, 20, 20));
			x += 40;
			if (x > 200) {
				x = 20;
				y += 40;
			}
		}
		layout.execute(jgxAdapter.getDefaultParent());
		//we find scale and shift factors to make it fit into canvas
		double smallestx=Double.MAX_VALUE;
		double greatestx=Double.MIN_VALUE;
		double smallesty=Double.MAX_VALUE;
		double greatesty=Double.MIN_VALUE;
	    for(mxICell cell : jgxAdapter.getVertexToCellMap().values()){
	    	if(jgxAdapter.getModel().getGeometry(cell).getX()<smallestx)
	    		smallestx=jgxAdapter.getModel().getGeometry(cell).getX();
	    	if(jgxAdapter.getModel().getGeometry(cell).getY()<smallesty)
	    		smallesty=jgxAdapter.getModel().getGeometry(cell).getY();
	    	if(jgxAdapter.getModel().getGeometry(cell).getX()>greatestx)
	    		greatestx=jgxAdapter.getModel().getGeometry(cell).getX();
	    	if(jgxAdapter.getModel().getGeometry(cell).getY()>greatesty)
	    		greatesty=jgxAdapter.getModel().getGeometry(cell).getY();
	    }
	    if(smallestx==greatestx)
	    	greatestx=smallestx+100;
	    if(smallesty==greatesty)
	    	greatesty=smallesty+100;
	    double sizex=canvas.getWidth()-40;//we keep 20 as margin
	    if(canvas==this.canvas)
	    	sizex=canvas.getWidth()-100;//more margin on molecule canvas
	    double sizey=canvas.getHeight()-40;//we keep 20 as margin
	    if(sizex<20 || sizey<20)
	    	return;
	    double scalex=1;
	    double scaley=1;
	    if(g.vertexSet().size()>1){
	    	scalex=sizex/(greatestx-smallestx);
	    	scaley=sizey/(greatesty-smallesty);
	    }
	    double shiftx=20-smallestx;
	    double shifty=20-smallesty;
	    //now we do the drawing
		for(DefaultEdge edge : g.edgeSet()){
			Process source  = g.getEdgeSource(edge);
			Process target = g.getEdgeTarget(edge);
			double sourcex=(jgxAdapter.getModel().getGeometry(jgxAdapter.getVertexToCellMap().get(source)).getX()-smallestx)*scalex+shiftx+20;
			double sourcey=(jgxAdapter.getModel().getGeometry(jgxAdapter.getVertexToCellMap().get(source)).getY()-smallesty)*scaley+shifty+20 ;
			double targetx=(jgxAdapter.getModel().getGeometry(jgxAdapter.getVertexToCellMap().get(target)).getX()-smallestx)*scalex+shiftx+20;
			double targety=(jgxAdapter.getModel().getGeometry(jgxAdapter.getVertexToCellMap().get(target)).getY()-smallesty)*scaley+shifty+20 ;
			drawArrow(canvas.getGraphicsContext2D(), sourcex, sourcey, targetx, targety, withhead);
		}
		processesmap.clear();
		for (mxICell cell : jgxAdapter.getVertexToCellMap().values()) {
			x=(jgxAdapter.getModel().getGeometry(cell).getX()-smallestx)*scalex+shiftx+20-10;
			y=(jgxAdapter.getModel().getGeometry(cell).getY()-smallesty)*scaley+shifty+20+5;
			canvas.getGraphicsContext2D().clearRect(x-2,y-12,20,15);
			String text="P"+jgxAdapter.getCellToVertexMap().get(cell).counter;
			if(!withhead)
				text=jgxAdapter.getCellToVertexMap().get(cell).toString();
			canvas.getGraphicsContext2D().fillText(text, x, y);
			List<Double> coords=new ArrayList<Double>();
			coords.add(x);
			coords.add(y);
			processesmap.put(jgxAdapter.getCellToVertexMap().get(cell),coords);
		}
	}
	
	void drawArrow(GraphicsContext gc, double sourcex, double sourcey, double targetx, double targety, boolean withhead) {
	    
	    double dx = targetx - sourcex, dy = targety - sourcey;
	    double angle = Math.atan2(dy, dx);
	    int len = (int) Math.sqrt(dx * dx + dy * dy);
	    len=len-10;

	    Transform transform = Transform.translate(sourcex, sourcey);
	    transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
	    gc.setTransform(new Affine(transform));

	    gc.strokeLine(0, 0, len, 0);
	    if(withhead)
	    	gc.fillPolygon(new double[]{len, len - 5, len - 5, len}, new double[]{0, -5, 5, 0}, 4);
	    gc.setTransform(new Affine());
	}
	
	private static class CanvasPane extends Pane {

        private final Canvas canvas;

        public CanvasPane(double width, double height) {
            canvas = new Canvas(width, height);
            getChildren().add(canvas);
        }

        public Canvas getCanvas() {
            return canvas;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            final double x = snappedLeftInset();
            final double y = snappedTopInset();
            final double w = snapSize(getWidth()) - x - snappedRightInset();
            final double h = snapSize(getHeight()) - y - snappedBottomInset();
            canvas.setLayoutX(x);
            canvas.setLayoutY(y);
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
    }
}
