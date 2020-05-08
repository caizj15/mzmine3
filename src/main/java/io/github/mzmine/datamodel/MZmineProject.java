/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.datamodel;

import java.io.File;
import java.util.Hashtable;

import io.github.mzmine.parameters.UserParameter;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;

/**
 *
 * MZmineProject collects all items user has opened or created during an MZmine session. This
 * includes
 * <ul>
 * <li>Experimental parameters and their values for each RawDataFile. Experimental parameters are
 * available for defining any properties of the sample, for instance concentration or a class label.
 * <li>Opened RawDataFiles
 * <li>PeakLists of each RawDataFile. A feature list represents results of feature detection on a
 * single RawDataFile or a processed version of a preceding PeakList.
 * <li>PeakLists of multiple aligned PeakLists. An aligned feature list represent results of
 * aligning multiple PeakLists of individual runs or a processed version of a preceding aligned
 * PeakList.
 * </ul>
 *
 * @see UserParameter
 * @see ParameterValue
 * @see RawDataFile
 * @see PeakList
 *
 */
public interface MZmineProject {

  /**
   * Return the filename of the project file
   */
  public File getProjectFile();

  /**
   * Adds a new experimental parameter to the project
   *
   * @param parameter
   */
  public void addParameter(UserParameter<?, ?> parameter);

  /**
   * Removes an experimental parameter from the project
   *
   * @param parameter
   */
  public void removeParameter(UserParameter<?, ?> parameter);

  /**
   * Returns true if project contains the experimental parameter
   */
  public boolean hasParameter(UserParameter<?, ?> parameter);

  /**
   * Returns all experimental parameter of the project
   */
  public UserParameter<?, ?>[] getParameters();

  public UserParameter<?,?> getParameterByName(String name);

  /**
   * Sets experimental parameter's value corresponding to a RawDataFile.
   * <p>
   * If the parameter does not exists in the project, it is added to the project. If parameter
   * already has a value corresponding the given file, previous value is replaced.
   *
   */
  public void setParameterValue(UserParameter<?, ?> parameter, RawDataFile rawDataFile,
      Object value);

  /**
   * Returns experimental parameter's value corresponding to a RawDataFile.
   *
   */
  public Object getParameterValue(UserParameter<?, ?> parameter, RawDataFile rawDataFile);

  /**
   * Adds a new RawDataFile to the project.
   */
  public void addFile(RawDataFile newFile);

  /**
   * Removes a RawDataFile from the project.
   */
  public void removeFile(RawDataFile file);

  /**
   * Returns all RawDataFiles of the project.
   *
   */
  public RawDataFile[] getDataFiles();

  /**
   * Adds a feature list to the project
   */
  public void addPeakList(PeakList peaklist);

//  /**
//   * Adds a mobilogram list to the project
//   */
//  public void addMobilogramList(MobilogramList mobilogramlist);

  /**
   * Removes a feature list from the project
   */
  public void removePeakList(PeakList peaklist);
//  /**
//   * Removes a mobilogram list from the project
//   */
//  public void removeMobilogramList(MobilogramList mobilogramlist);

  /**
   * Returns all feature lists of the project
   */
  public PeakList[] getPeakLists();

  public ObservableList<RawDataFile> getRawDataFiles();

  public ListProperty<RawDataFile> rawDataFilesProperty();

  public ObservableList<PeakList> getFeatureLists();

  public ListProperty<PeakList> featureListsProperty();

  /**
   * Returns all feature lists which contain given data file
   */
  public PeakList[] getPeakLists(RawDataFile file);

  public Hashtable<UserParameter<?, ?>, Hashtable<RawDataFile, Object>> getProjectParametersAndValues();

  public void setProjectParametersAndValues(Hashtable<UserParameter<?, ?>, Hashtable<RawDataFile, Object>> projectParametersAndValues);

  public MobilogramList[] getMobilogramLists();

  /**
   * Returns all mobilogram lists which contain given data file
   */
  public MobilogramList[] getMobilogramLists(RawDataFile file);


    // public void notifyObjectChanged(Object object, boolean structureChanged);

   // public void addProjectListener(MZmineProjectListener newListener);

  //public void removeProjectListener(MZmineProjectListener listener);

}
