/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.datamodel.data.types.numbers;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import io.github.mzmine.datamodel.data.types.modifiers.EditableColumnType;
import io.github.mzmine.datamodel.data.types.modifiers.StringParser;
import io.github.mzmine.main.MZmineCore;

public class MZType extends NumberType<Double> implements EditableColumnType, StringParser<MZType> {
  // only used in cases where the mzmine config has no format
  private static final NumberFormat DEFAULT_FORMAT = new DecimalFormat("0.0000");

  public MZType(Double value) {
    super(value);
  }

  @Override
  public NumberFormat getFormatter() {
    try {
      return MZmineCore.getConfiguration().getMZFormat();
    } catch (NullPointerException e) {
      // only happens if types are used without initializing the MZmineCore
      return DEFAULT_FORMAT;
    }
  }

  @Override
  public String getHeaderString() {
    return "m/z";
  }

  @Override
  public MZType fromString(String s) {
    try {
      return new MZType(Double.parseDouble(s));
    } catch (Exception e) {
      logger.log(Level.WARNING, "Cannot convert String to Double: " + s);
      return null;
    }
  }
}
