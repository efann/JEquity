/*
 * J'EquityFX
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequityfx.controller;

import com.beowurks.jequityfx.dao.tableview.EnvironmentProperty;
import com.beowurks.jequityfx.main.Main;
import com.beowurks.jequityfx.utility.Misc;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class AboutBoxController implements EventHandler<ActionEvent>
{
  private final static String JEQUITYFX_HOME = "http://www.beowurks.com/applications/single/J'Equity";
  private final static String JEQUITYFX_LICENSE = "https://opensource.org/licenses/EPL-1.0";

  private final ObservableList<EnvironmentProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private Hyperlink lnkLogo;

  @FXML
  private Hyperlink lnkLicense;

  @FXML
  private Hyperlink lnkApplicationName;

  @FXML
  private Label lblApplicationVersion;

  @FXML
  private Label lblCopyright;

  @FXML
  private TableView tblEnvironment;

  @FXML
  private TableColumn colKey;

  @FXML
  private TableColumn colValue;


  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/34785417/javafx-fxml-controller-constructor-vs-initialize-method
  @FXML
  public void initialize()
  {
    this.lnkLicense.setText("Licensed under https://opensource.org/licenses/EPL-1.0");
    this.lnkLicense.setOnAction(this);

    this.lnkApplicationName.setText(Main.getApplicationName());
    this.lnkApplicationName.setOnAction(this);

    this.lblApplicationVersion.setText(Main.getApplicationVersion());
    this.lblCopyright.setText(String.format("Copyright© 2008-%d Beowurks. All rights reserved.", Calendar.getInstance().get(Calendar.YEAR)));

    Image loImage = new Image("/com/beowurks/jequityfx/view/images/JEquity.jpg");
    this.lnkLogo.setGraphic(new ImageView(loImage));
    this.lnkLogo.setOnAction(this);

    this.setupTable();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void handle(ActionEvent toEvent)
  {
    Object loSource = toEvent.getSource();
    if (loSource instanceof Hyperlink)
    {
      Hyperlink loHyperLink = (Hyperlink) loSource;
      String lcURL = AboutBoxController.JEQUITYFX_HOME;
      // If not a graphic image. . . .
      if (loHyperLink.getGraphic() == null)
      {
        String lcText = loHyperLink.getText();
        if (lcText.toLowerCase().indexOf("licensed") != -1)
        {
          lcURL = AboutBoxController.JEQUITYFX_LICENSE;
        }
      }

      Main.getMainHostServices().showDocument(lcURL);
    }
  }

  // ---------------------------------------------------------------------------------------------------------------------
  private void setupTable()
  {
    this.tblEnvironment.setEditable(false);

    // "key" relates to EnvironmentProperty.keyProperty and "value" relates to EnvironmentProperty.valueProperty
    this.colKey.setCellValueFactory(new PropertyValueFactory<EnvironmentProperty, String>("key"));
    this.colValue.setCellValueFactory(new PropertyValueFactory<EnvironmentProperty, String>("value"));

    final Enumeration<?> loEnum = System.getProperties().propertyNames();
    final StringBuilder lcValue = new StringBuilder();

    while (loEnum.hasMoreElements())
    {
      final String lcKey = loEnum.nextElement().toString();

      Misc.clearStringBuilder(lcValue);
      lcValue.append(System.getProperty(lcKey));
      int lnPos;

      if ((lnPos = lcValue.toString().indexOf(0x0A)) != -1)
      {
        lcValue.replace(lnPos, lnPos + 1, "\\n");
      }

      if ((lnPos = lcValue.toString().indexOf(0x0D)) != -1)
      {
        lcValue.replace(lnPos, lnPos + 1, "\\r");
      }

      this.foDataList.add(new EnvironmentProperty(lcKey, lcValue.toString()));
    }

    this.tblEnvironment.setItems(this.foDataList);
    this.tblEnvironment.setColumnResizePolicy((param -> true));

    Platform.runLater(() -> this.customResize(this.tblEnvironment));

  }

  // ---------------------------------------------------------------------------------------------------------------------
  // From https://stackoverflow.com/questions/14650787/javafx-column-in-tableview-auto-fit-size
  private void customResize(TableView<?> toTableView)
  {
    AtomicLong loWidth = new AtomicLong();
    toTableView.getColumns().forEach(loColumn -> {
      loWidth.addAndGet((long) loColumn.getWidth());
    });
    double lnTableWidth = toTableView.getWidth();

    if (lnTableWidth > loWidth.get())
    {
      toTableView.getColumns().forEach(col -> {
        col.setPrefWidth(col.getWidth() + ((lnTableWidth - loWidth.get()) / toTableView.getColumns().size()));
      });
    }
  }
  // ---------------------------------------------------------------------------------------------------------------------

}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
