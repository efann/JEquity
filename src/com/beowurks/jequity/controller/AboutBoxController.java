/*
 * J'Equity
 * Copyright(c) 2008-2017
 * Original Author: Eddie Fann
 * License: Eclipse Public License
 *
 */

package com.beowurks.jequity.controller;

import com.beowurks.jequity.dao.tableview.EnvironmentProperty;
import com.beowurks.jequity.main.Main;
import com.beowurks.jequity.utility.Misc;
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
import org.controlsfx.control.HyperlinkLabel;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class AboutBoxController implements EventHandler<ActionEvent>
{
  private final static String JEQUITY_COMPANY = "http://www.beowurks.com/";
  private final static String JEQUITY_HOME = "http://www.beowurks.com/applications/single/J'Equity";
  private final static String JEQUITY_LICENSE = "https://opensource.org/licenses/EPL-1.0";

  private final ObservableList<EnvironmentProperty> foDataList = FXCollections.observableArrayList();

  @FXML
  private Hyperlink lnkLogo;

  @FXML
  private HyperlinkLabel lnkLicense;

  @FXML
  private Hyperlink lnkApplicationName;

  @FXML
  private Label lblApplicationVersion;

  @FXML
  private HyperlinkLabel lnkCopyright;

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
    this.lnkLicense.setText("Licensed under the [Eclipse Public License 1.0]");
    this.lnkLicense.setOnAction(this);

    this.lnkApplicationName.setText(Main.getApplicationName());
    this.lnkApplicationName.setOnAction(this);

    this.lblApplicationVersion.setText(Main.getApplicationVersion());
    this.lnkCopyright.setText(String.format("Copyright© 2008-%d [Beowurks]. All rights reserved.", Calendar.getInstance().get(Calendar.YEAR)));
    this.lnkCopyright.setOnAction(this);

    final Image loImage = new Image("/com/beowurks/jequity/view/images/JEquity.jpg");
    this.lnkLogo.setGraphic(new ImageView(loImage));
    this.lnkLogo.setOnAction(this);

    this.setupTable();
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void handle(final ActionEvent toEvent)
  {
    final Object loSource = toEvent.getSource();
    if (loSource instanceof Hyperlink)
    {
      final Hyperlink loHyperLink = (Hyperlink) loSource;
      String lcURL = AboutBoxController.JEQUITY_HOME;
      // If not a graphic image. . . .
      if (loHyperLink.getGraphic() == null)
      {
        final String lcText = loHyperLink.getText();

        if (lcText.toLowerCase().indexOf("license") != -1)
        {
          lcURL = AboutBoxController.JEQUITY_LICENSE;
        }
        else if (lcText.toLowerCase().indexOf("beowurks") != -1)
        {
          lcURL = AboutBoxController.JEQUITY_COMPANY;
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
  private void customResize(final TableView<?> toTableView)
  {
    final AtomicLong loWidth = new AtomicLong();
    toTableView.getColumns().forEach(loColumn -> {
      loWidth.addAndGet((long) loColumn.getWidth());
    });
    final double lnTableWidth = toTableView.getWidth();

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
