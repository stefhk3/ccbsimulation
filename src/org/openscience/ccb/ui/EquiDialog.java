package org.openscience.ccb.ui;

import java.util.Optional;

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import javafx.util.Pair;

public class EquiDialog extends Dialog<Pair<String, String>>{
	
	Optional<Pair<String, String>> result;
	
	public EquiDialog(Optional<Pair<String, String>> result){
		this.setTitle("Chemical process equivalence");
		this.setHeaderText("Enter two processes");
	
		TextField p1 = new TextField();
		p1.setPrefWidth(200);
		TextField p2 = new TextField();
		p2.setPrefWidth(200);
		
		FlowPane flow = new FlowPane(Orientation.VERTICAL);
		flow.setColumnHalignment(HPos.LEFT); // align labels on left
	    flow.setPrefWrapLength(100); // preferred height = 200
	    flow.setPrefHeight(100);
	    flow.getChildren().add(p1);
	    flow.getChildren().add(p2);
		this.getDialogPane().setContent(flow);
				
		final ButtonType buttonTypeOk = new ButtonType("Ok", ButtonData.OK_DONE);
		final ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		this.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);
	
		this.setResultConverter(new Callback<ButtonType, Pair<String, String>>() {
		    @Override
		    public Pair<String, String> call(ButtonType b) {
	
		        if (b == buttonTypeOk) {
		        	return new Pair<>(p1.getText(), p2.getText());
		        }
	
		        return null;
		    }
		});
				
		result = this.showAndWait();
	}
}