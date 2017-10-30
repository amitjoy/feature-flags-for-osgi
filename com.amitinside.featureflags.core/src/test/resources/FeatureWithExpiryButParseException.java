
/*******************************************************************************
 * Copyright (c) 2017 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
import com.amitinside.featureflags.annotation.ExpirationType;
import com.amitinside.featureflags.annotation.Feature;

public final class FeatureWithExpiryButParseException {

    @Feature(expirationType = ExpirationType.TIMED, expirationDatePattern = "DUMMY")
    public static final String MY_FEATURE = "feature.identifier";

}
