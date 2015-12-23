/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2015 Broad Institute, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.gui;

import juicebox.HiCGlobals;
import juicebox.data.HiCFileTools;
import juicebox.mapcolorui.Feature2DHandler;
import juicebox.mapcolorui.FeatureRenderer;
import juicebox.state.SaveFileDialog;
import juicebox.track.LoadAction;
import juicebox.track.LoadEncodeAction;
import juicebox.track.feature.CustomAnnotation;
import juicebox.track.feature.CustomAnnotationHandler;
import juicebox.windowui.RecentMenu;
import juicebox.windowui.SaveAnnotationsDialog;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by muhammadsaadshamim on 8/4/15.
 */
public class MainMenuBar {
    private static final int recentMapListMaxItems = 10;
    private static final int recentLocationMaxItems = 20;
    private static final String recentMapEntityNode = "hicMapRecent";
    private static final String recentLocationEntityNode = "hicLocationRecent";
    private static final String recentStateEntityNode = "hicStateRecent";
    private static final Logger log = Logger.getLogger(MainMenuBar.class);
    public static JMenuItem exportAnnotationsMI;
    public static JMenuItem undoMenuItem;
    //meh - public static ArrayList<CustomAnnotation> customAnnotations;
    public static CustomAnnotation customAnnotations;
    public static CustomAnnotationHandler customAnnotationHandler;
    private static JMenuItem loadLastMI;
    private static RecentMenu recentMapMenu;
    private static RecentMenu recentLocationMenu;
    private static JMenuItem saveLocationList;
    private static JMenuItem saveStateForReload;
    private static RecentMenu previousStates;
    private static JMenuItem exportMapAsFile;
    private static JMenuItem importMapAsFile;
    private static JMenuItem slideShow;
    private static File temp;
    private static boolean unsavedEdits;
    private static JMenu annotationsMenu;
    private static LoadEncodeAction encodeAction;
    private static LoadAction trackLoadAction;
    private final File fileForExport = new File(HiCGlobals.xmlSavedStatesFileName);
    // created separately because it will be enabled after an initial map is loaded
    private final JMenuItem loadControlFromList = new JMenuItem();
    private File currentStates = new File("testStates");
    private JCheckBoxMenuItem showLoopsItem;

    public LoadAction getTrackLoadAction() {
        return trackLoadAction;
    }

    public LoadEncodeAction getEncodeAction() {
        return encodeAction;
    }

    public boolean unsavedEditsExist() {
        String tempPath = "/unsaved-hiC-annotations1";
        temp = HiCFileTools.openTempFile(tempPath);
        unsavedEdits = temp.exists();
        return unsavedEdits;
    }

    public void addRecentMapMenuEntry(String title, boolean status) {
        recentMapMenu.addEntry(title, status);
    }

    public void addRecentStateMenuEntry(String title, boolean status) {
        recentLocationMenu.addEntry(title, status);
    }

    public void initializeCustomAnnotations() {
        // meh - customAnnotations = new ArrayList<CustomAnnotation>();
        customAnnotations = new CustomAnnotation("1");
        customAnnotationHandler = new CustomAnnotationHandler();
    }

    public JMenuBar createMenuBar(final SuperAdapter superAdapter) {

        JMenuBar menuBar = new JMenuBar();

        //======== fileMenu ========
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        //---- openMenuItem ----

        // create control first because it is enabled by regular open
        loadControlFromList.setText("Open Control...");
        loadControlFromList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                superAdapter.loadFromListActionPerformed(true);
            }
        });
        loadControlFromList.setEnabled(false);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                superAdapter.loadFromListActionPerformed(false);
            }
        });
        fileMenu.add(openItem);
        fileMenu.add(loadControlFromList);

        recentMapMenu = new RecentMenu("Open Recent", recentMapListMaxItems, recentMapEntityNode, HiCGlobals.menuType.MAP, false) {

            private static final long serialVersionUID = 4202L;

            public void onSelectPosition(String mapPath) {
                String delimiter = "@@";
                String[] temp;
                temp = mapPath.split(delimiter);
                //initProperties();         // don't know why we're doing this here
                superAdapter.loadFromRecentActionPerformed((temp[1]), (temp[0]), false);
            }
        };
        recentMapMenu.setMnemonic('R');


        fileMenu.add(recentMapMenu);
        fileMenu.addSeparator();

        JMenuItem showStats = new JMenuItem("Show Dataset Metrics");
        showStats.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                superAdapter.showDataSetMetrics();
            }
        });


        fileMenu.add(showStats);
        fileMenu.addSeparator();

        JMenuItem saveToImage = new JMenuItem();
        saveToImage.setText("Export Image...");
        saveToImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                superAdapter.launchExportImage();
            }
        });
        fileMenu.add(saveToImage);

        // TODO: make this an export of the data on screen instead of a GUI for CLT
        if (!HiCGlobals.isRestricted) {
            JMenuItem dump = new JMenuItem("Export Data...");
            dump.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    superAdapter.exportDataLauncher();
                }
            });
            fileMenu.add(dump);
        }

        JMenuItem creditsMenu = new JMenuItem();
        creditsMenu.setText("About");
        creditsMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/images/juicebox.png"));
                JLabel iconLabel = new JLabel(icon);
                JPanel iconPanel = new JPanel(new GridBagLayout());
                iconPanel.add(iconLabel);

                JPanel textPanel = new JPanel(new GridLayout(0, 1));
                textPanel.add(new JLabel("<html><center>" +
                        "<h2 style=\"margin-bottom:30px;\" class=\"header\">" +
                        "Juicebox: Visualization software for Hi-C data" +
                        "</h2>" +
                        "</center>" +
                        "<p>" +
                        "Juicebox is Aiden Lab's software for visualizing data from proximity ligation experiments, such as Hi-C, 5C, and Chia-PET.<br>" +
                        "Juicebox was created by Jim Robinson, Neva C. Durand, and Erez Aiden. Ongoing development work is carried out by Neva C. Durand,<br>" +
                        "Muhammad Shamim, and Ido Machol.<br><br>" +
                        "Copyright © 2014. Broad Institute and Aiden Lab" +
                        "<br><br>" +
                        "If you use Juicebox in your research, please cite:<br><br>" +
                        "<strong>Suhas S.P. Rao*, Miriam H. Huntley*, Neva C. Durand, Elena K. Stamenova, Ivan D. Bochkov, James T. Robinson,<br>" +
                        "Adrian L. Sanborn, Ido Machol, Arina D. Omer, Eric S. Lander, Erez Lieberman Aiden.<br>" +
                        "\"A 3D Map of the Human Genome at Kilobase Resolution Reveals Principles of Chromatin Looping.\" <em>Cell</em> 159, 2014.</strong><br>" +
                        "* contributed equally" +
                        "</p></html>"));

                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.add(textPanel);
                mainPanel.add(iconPanel, BorderLayout.WEST);

                JOptionPane.showMessageDialog(superAdapter.getMainWindow(), mainPanel, "About", JOptionPane.PLAIN_MESSAGE);//INFORMATION_MESSAGE
            }
        });
        fileMenu.add(creditsMenu);

        //---- exit ----
        JMenuItem exit = new JMenuItem();
        exit.setText("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                superAdapter.exitActionPerformed();
            }
        });
        fileMenu.add(exit);

        // "Annotations" menu items
        annotationsMenu = new JMenu("Annotations");

        JMenuItem newLoadMI = new JMenuItem();

        trackLoadAction = superAdapter.createNewTrackLoadAction();
        newLoadMI.setAction(trackLoadAction);
        annotationsMenu.add(newLoadMI);

        JMenuItem loadEncodeMI = new JMenuItem();
        encodeAction = superAdapter.createNewLoadEncodeAction();
        loadEncodeMI.setAction(encodeAction);
        annotationsMenu.add(loadEncodeMI);

        // TODO - this is never added to a menu...
        JMenuItem loadFromURLItem = new JMenuItem("Load Annotation from URL...");
        loadFromURLItem.addActionListener(new AbstractAction() {

            private static final long serialVersionUID = 4203L;

            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.loadFromURLActionPerformed();
            }
        });

        final JMenu feature2DPlottingOptions = new JMenu("2D Annotations");
        showLoopsItem = new JCheckBoxMenuItem("Show");
        showLoopsItem.setSelected(true);
        showLoopsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.setShowLoops(showLoopsItem.isSelected());
                superAdapter.repaint();
            }
        });
        showLoopsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

        final JMenu featureRenderingOptions = new JMenu("Partial Plotting");
        final JCheckBoxMenuItem renderFullFeatureItem = new JCheckBoxMenuItem("Full Feature");
        final JCheckBoxMenuItem renderLLFeatureItem = new JCheckBoxMenuItem("Lower Left");
        final JCheckBoxMenuItem renderURFeatureItem = new JCheckBoxMenuItem("Upper Right");
        renderFullFeatureItem.setSelected(true);
        FeatureRenderer.enablePlottingOption = FeatureRenderer.PlottingOption.EVERYTHING;

        renderFullFeatureItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FeatureRenderer.enablePlottingOption = FeatureRenderer.PlottingOption.EVERYTHING;
                renderFullFeatureItem.setSelected(true);
                renderLLFeatureItem.setSelected(false);
                renderURFeatureItem.setSelected(false);
                superAdapter.repaint();
            }
        });
        renderLLFeatureItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FeatureRenderer.enablePlottingOption = FeatureRenderer.PlottingOption.ONLY_LOWER_LEFT;
                renderFullFeatureItem.setSelected(false);
                renderLLFeatureItem.setSelected(true);
                renderURFeatureItem.setSelected(false);
                superAdapter.repaint();
            }
        });
        renderURFeatureItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FeatureRenderer.enablePlottingOption = FeatureRenderer.PlottingOption.ONLY_UPPER_RIGHT;
                renderFullFeatureItem.setSelected(false);
                renderLLFeatureItem.setSelected(false);
                renderURFeatureItem.setSelected(true);
                superAdapter.repaint();
            }
        });

        featureRenderingOptions.add(renderFullFeatureItem);
        featureRenderingOptions.add(renderLLFeatureItem);
        featureRenderingOptions.add(renderURFeatureItem);

        final JCheckBoxMenuItem toggleSparse2DFeaturePlotting = new JCheckBoxMenuItem("Plot Sparse:");
        toggleSparse2DFeaturePlotting.setSelected(false);
        toggleSparse2DFeaturePlotting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.setSparseFeaturePlotting(toggleSparse2DFeaturePlotting.isSelected());
                superAdapter.repaint();
            }
        });
        toggleSparse2DFeaturePlotting.setToolTipText("Plot a limited number of 2D annotations at a time\n(speed up plotting when there are many annotations).");
        toggleSparse2DFeaturePlotting.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));

        final JTextField numSparse = new JTextField("" + Feature2DHandler.numberOfLoopsToFind);
        numSparse.setEnabled(true);
        numSparse.isEditable();
        numSparse.setToolTipText("Set how many 2D annotations to plot at a time.");


        final JButton updateSparseOptions = new JButton("Update");
        updateSparseOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (numSparse.getText().length() > 0) {
                    Feature2DHandler.numberOfLoopsToFind = Integer.parseInt(numSparse.getText());
                }
            }
        });
        updateSparseOptions.setToolTipText("Set how many 2D annotations to plot at a time.");

        JPanel sparseOptions = new JPanel();
        sparseOptions.setLayout(new GridLayout(0, 2));
        sparseOptions.add(numSparse);
        sparseOptions.add(updateSparseOptions);
        sparseOptions.setBackground(toggleSparse2DFeaturePlotting.getBackground());
        sparseOptions.setToolTipText("Set how many 2D annotations to plot at a time.");


        final JCheckBoxMenuItem enlarge2DFeatures = new JCheckBoxMenuItem("Enlarge");
        enlarge2DFeatures.setSelected(false);
        enlarge2DFeatures.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.enlarge2DFeaturePlotting(enlarge2DFeatures.isSelected());
                superAdapter.repaint();
            }
        });
        enlarge2DFeatures.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));

        final JCheckBoxMenuItem toggle2DFeatureOpacity = new JCheckBoxMenuItem("Translucent");
        toggle2DFeatureOpacity.setSelected(false);
        toggle2DFeatureOpacity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.toggleFeatureOpacity(toggle2DFeatureOpacity.isSelected());
                superAdapter.repaint();
            }
        });
        toggle2DFeatureOpacity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));

        final JCheckBoxMenuItem showCustomLoopsItem = new JCheckBoxMenuItem("Show");

        showCustomLoopsItem.setSelected(true);
        showCustomLoopsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customAnnotations.setShowCustom(showCustomLoopsItem.isSelected());
                superAdapter.repaint();
            }
        });
        showCustomLoopsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

        final JMenuItem editVisibleMI = new JMenuItem("Copy to Hand Annotations");
        editVisibleMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customAnnotations = superAdapter.addVisibleLoops(customAnnotationHandler, customAnnotations);
                showLoopsItem.setSelected(false);
                showCustomLoopsItem.setSelected(true);
                superAdapter.setShowLoops(false);
                superAdapter.repaint();
            }
        });

        feature2DPlottingOptions.add(showLoopsItem);
        feature2DPlottingOptions.add(enlarge2DFeatures);
        feature2DPlottingOptions.add(toggle2DFeatureOpacity);
        feature2DPlottingOptions.add(featureRenderingOptions);
        feature2DPlottingOptions.add(editVisibleMI);

        // use hidden hotkey instead of plot sparse button
        if (HiCGlobals.showSparsePlottingOptions) {
            feature2DPlottingOptions.addSeparator();
            feature2DPlottingOptions.add(toggleSparse2DFeaturePlotting);
            feature2DPlottingOptions.add(sparseOptions);
        }
        annotationsMenu.add(feature2DPlottingOptions);
        annotationsMenu.setEnabled(false);


        // Annotations Menu Items
        final JMenu customAnnotationMenu = new JMenu("Hand Annotations");
        exportAnnotationsMI = new JMenuItem("Export...");
        final JMenuItem exportOverlapMI = new JMenuItem("Export Overlap...");
        loadLastMI = new JMenuItem("Load Last Session");
        undoMenuItem = new JMenuItem("Undo Annotation");
        final JMenuItem clearCurrentMI = new JMenuItem("Clear All");

        // Annotate Item Actions
        exportAnnotationsMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.exportAnnotations();
            }
        });

        exportOverlapMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.exportOverlapMIAction(customAnnotations);
            }
        });

        loadLastMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customAnnotations = superAdapter.generateNewCustomAnnotation(temp, "1");
                temp.delete();
                loadLastMI.setEnabled(false);
                exportAnnotationsMI.setEnabled(true);
            }
        });

        clearCurrentMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = superAdapter.clearCustomAnnotationDialog();

                if (n == JOptionPane.YES_OPTION) {
                    //TODO: do something with the saving... just update temp?
                    customAnnotations.clearAnnotations();
                    exportAnnotationsMI.setEnabled(false);
                    loadLastMI.setEnabled(false);
                    superAdapter.repaint();
                }
            }
        });

        undoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customAnnotationHandler.undo(customAnnotations);
                superAdapter.repaint();
            }
        });
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0));

        //Add annotate menu items
        customAnnotationMenu.add(showCustomLoopsItem);
        customAnnotationMenu.add(exportAnnotationsMI);
        //customAnnotationMenu.add(exportOverlapMI);
        customAnnotationMenu.add(undoMenuItem);
        customAnnotationMenu.add(clearCurrentMI);
        if (unsavedEditsExist()) {
            customAnnotationMenu.add(new JSeparator());
            customAnnotationMenu.add(loadLastMI);
            loadLastMI.setEnabled(true);
        }

        exportAnnotationsMI.setEnabled(false);
        undoMenuItem.setEnabled(false);

        annotationsMenu.add(customAnnotationMenu);
        // TODO: Semantic inconsistency between what user sees (loop) and back end (peak) -- same thing.


        JMenu bookmarksMenu = new JMenu("Bookmarks");
        //---- Save location ----
        saveLocationList = new JMenuItem();
        saveLocationList.setText("Save current location");
        saveLocationList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //code to add a recent location to the menu
                String stateString = superAdapter.getLocationDescription();
                String stateDescription = superAdapter.getDescription("location");
                if (null != stateDescription && stateDescription.length() > 0) {
                    superAdapter.addRecentStateMenuEntry(stateDescription + "@@" + stateString, true);
                    recentLocationMenu.setEnabled(true);
                }
            }
        });
        bookmarksMenu.add(saveLocationList);
        saveLocationList.setEnabled(false);
        //---Save State test-----
        saveStateForReload = new JMenuItem();
        saveStateForReload.setText("Save current state");
        saveStateForReload.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //code to add a recent location to the menu
                try{
                String stateDescription = superAdapter.getDescription("state");
                if (stateDescription != null && stateDescription.length() > 0) {
                    stateDescription = previousStates.checkForDuplicateNames(stateDescription);
                    if (stateDescription == null || stateDescription.length() < 0) {
                        return;
                    }
                    previousStates.addEntry(stateDescription, true);
                    superAdapter.addNewStateToXML(stateDescription);
                }
                    previousStates.setEnabled(true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        saveStateForReload.setEnabled(false);
        bookmarksMenu.add(saveStateForReload);

        recentLocationMenu = new RecentMenu("Restore saved location", recentLocationMaxItems, recentLocationEntityNode, HiCGlobals.menuType.LOCATION, true) {

            private static final long serialVersionUID = 4204L;

            public void onSelectPosition(String mapPath) {
                String delimiter = "@@";
                String[] temp;
                temp = mapPath.split(delimiter);
                superAdapter.restoreLocation(temp[1]);
                superAdapter.setNormalizationDisplayState();

            }
        };
        recentLocationMenu.setMnemonic('S');
        recentLocationMenu.setEnabled(false);
        bookmarksMenu.add(recentLocationMenu);

        previousStates = new RecentMenu("Restore previous states", recentLocationMaxItems, recentStateEntityNode, HiCGlobals.menuType.STATE, true) {

            private static final long serialVersionUID = 4205L;

            public void onSelectPosition(String mapPath) {
                superAdapter.launchLoadStateFromXML(mapPath);
            }

        };

        bookmarksMenu.add(previousStates);

        //---Export Menu-----
        JMenu shareMenu = new JMenu("Share States");

        //---Export Maps----
        exportMapAsFile = new JMenuItem();
        exportMapAsFile.setText("Export Saved States");
        exportMapAsFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SaveFileDialog(fileForExport);
            }
        });


        //---Import Maps----
        importMapAsFile = new JMenuItem();
        importMapAsFile.setText("Import State From File");
        importMapAsFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.launchImportState(fileForExport);
                importMapAsFile.setSelected(true);
            }
        });


        //---Slideshow----
        //ALL YOUR'S MARIE
        slideShow = new JMenuItem();
        slideShow.setText("View Slideshow");
        slideShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                superAdapter.launchSlideShow();
                HiCGlobals.slideshowEnabled = true;
            }
        });
        //bookmarksMenu.add(slideShow);


        //Add menu items
        //shareMenu.add(exportMapAsFile);
        //shareMenu.add(importMapAsFile);

        bookmarksMenu.addSeparator();
        bookmarksMenu.add(exportMapAsFile);
        bookmarksMenu.add(importMapAsFile);
        /*
        //---3D Model Menu-----
        JMenu toolsMenu = new JMenu("Tools");
        //---Export Maps----
        JMenuItem launch3DModel = new JMenuItem();
        launch3DModel.setText("Visualize 3D Model");
        launch3DModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Launcher demo = new Launcher();
                demo.setVisible(true);
            }
        });
        toolsMenu.add(launch3DModel);
        */

        menuBar.add(fileMenu);
        menuBar.add(annotationsMenu);
        menuBar.add(bookmarksMenu);
        //menuBar.add(shareMenu);
        //menuBar.add(toolsMenu);
        return menuBar;
    }

    public void setShow2DAnnotations(boolean show){
        showLoopsItem.setSelected(show);
    }

    public void clearAllAnnotations(){
        customAnnotations.clearAnnotations();
    }

    public void deleteUnsavedEdits() {
        customAnnotations.deleteTempFile();
    }

    public void setEnableForAllElements(boolean status) {
        annotationsMenu.setEnabled(status);
        saveLocationList.setEnabled(status);
        saveStateForReload.setEnabled(status);
        saveLocationList.setEnabled(status);
    }

    public void updatePrevStateNameFromImport(String path) {
        previousStates.updateNamesFromImport(path);
    }

    public void setContolMapLoadableEnabled(boolean status) {
        loadControlFromList.setEnabled(status);
    }
}
