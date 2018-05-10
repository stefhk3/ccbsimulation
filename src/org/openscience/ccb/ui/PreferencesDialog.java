package org.openscience.ccb.ui;

import java.util.Optional;

import org.openscience.ccb.util.CCBConfiguration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

public class PreferencesDialog extends Dialog<CCBConfiguration>{
	
	Optional<CCBConfiguration> result;
	
	public PreferencesDialog(final CCBConfiguration ccbconfiguration){
		this.setTitle("CCB preferencees");
		this.setHeaderText("Change your CCB preferences here");
	
		Label label1 = new Label("Allow spontaneous breaks: ");
		ObservableList<String> options = 
			    FXCollections.observableArrayList(
			        "yes",
			        "no"
			    );
		final ComboBox<String> comboBox1 = new ComboBox<String>(options);
		if(ccbconfiguration.allowSpontaneousBreaks)
			comboBox1.setValue("yes");
		else
			comboBox1.setValue("no");
		Label label2 = new Label("Force move: ");
		ObservableList<String> options2 = 
			    FXCollections.observableArrayList(
			        "yes",
			        "no"
			    );
		final ComboBox<String> comboBox2 = new ComboBox<String>(options2);
		if(ccbconfiguration.forceMove)
			comboBox2.setValue("yes");
		else
			comboBox2.setValue("no");
		Label label3 = new Label("Force promotion: ");
		ObservableList<String> options3 = 
			    FXCollections.observableArrayList(
			        "yes",
			        "no"
			    );
		final ComboBox<String> comboBox3 = new ComboBox<String>(options3);
		if(ccbconfiguration.forcePromotion)
			comboBox3.setValue("yes");
		else
			comboBox3.setValue("no");
				
		GridPane grid = new GridPane();
		grid.add(label1, 1, 1);
		grid.add(comboBox1, 2, 1);
		grid.add(label2, 1, 2);
		grid.add(comboBox2, 2, 2);
		grid.add(label3, 1, 3);
		grid.add(comboBox3, 2, 3);
		this.getDialogPane().setContent(grid);
				
		final ButtonType buttonTypeOk = new ButtonType("Ok", ButtonData.OK_DONE);
		final ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		this.getDialogPane().getButtonTypes().addAll(buttonTypeCancel, buttonTypeOk);
	
		this.setResultConverter(new Callback<ButtonType, CCBConfiguration>() {
		    @Override
		    public CCBConfiguration call(ButtonType b) {
	
		        if (b == buttonTypeOk) {
		        	ccbconfiguration.allowSpontaneousBreaks=comboBox1.getValue().equals("yes");
		            return ccbconfiguration;
		        }
	
		        return null;
		    }
		});
				
		result = this.showAndWait();
				
		/*if (result.isPresent()) {
	
		    actionStatus.setText("Result: " + result.get());
		}*/
	}
}
