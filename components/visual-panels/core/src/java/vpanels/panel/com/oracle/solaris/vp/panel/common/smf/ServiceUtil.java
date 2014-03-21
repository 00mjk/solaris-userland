/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 */

package com.oracle.solaris.vp.panel.common.smf;

import java.text.DateFormat;
import java.util.*;
import com.oracle.solaris.rad.client.ADRName;
import com.oracle.solaris.scf.common.FMRI;
import com.oracle.solaris.vp.panel.common.api.smf_old.ServiceInfo;
import com.oracle.solaris.vp.panel.common.api.smf_old.SmfState;
import com.oracle.solaris.vp.panel.common.model.ManagedObjectStatus;
import com.oracle.solaris.vp.util.misc.finder.Finder;

public class ServiceUtil {
    //
    // Static data
    //

    public static final String SERVICE_DOMAIN =
	"com.oracle.solaris.vp.panel.common.api.smf_old";

    //
    // Static methods
    //

    public static ADRName getServiceObjectName(
	String service, String instance) {
	Map<String, String> kvs = new HashMap<String, String>();
	kvs.put("type", "ServiceInfo");
	kvs.put("service", service);
	kvs.put("instance", instance == null ? "" : instance);
	return (new ADRName(SERVICE_DOMAIN, kvs));
    }

    public static ADRName toADRName(String service, String instance) {
	try {
		return (getServiceObjectName(service, instance));
	} catch (Exception e) {
		throw (new IllegalArgumentException(e));
	}
    }

    public static ADRName toADRName(FMRI fmri) {
	switch (fmri.getSvcType()) {
	case INSTANCE:
	    return (toADRName(fmri.getService(), fmri.getInstance()));
	case SERVICE:
	    return (toADRName(fmri.getService(), null));
	default:
	    return (null);
	}
    }

    public static String toFMRI(ADRName an) {
	String domain = an.getDomain();
	Map<String, String> kvs = an.getKVPairs();
	String service = kvs.get("service");
	String instance = kvs.get("instance");
	if (!domain.equals(SERVICE_DOMAIN) || service == null)
		throw new IllegalArgumentException("Not an SMF Object name");
	return ("svc:/" + (instance.equals("") ?
	    service : service + ":" + instance));
    }

    public static String toService(ADRName an) {
	String domain = an.getDomain();
	Map<String, String> kvs = an.getKVPairs();
	String service = kvs.get("service");
	if (domain.equals(SERVICE_DOMAIN) && service != null)
		return service;
	else
		throw new IllegalArgumentException("Not an SMF Object name");
    }

    public static ManagedObjectStatus getPanelStatus(SmfState state) {
	ManagedObjectStatus status = ManagedObjectStatus.ERROR;

	if (state != null) {
	    switch (state) {
		case ONLINE:
		case OFFLINE:
		case DISABLED:
		    status = ManagedObjectStatus.HEALTHY;
		    break;

		case UNINIT:
		case DEGRADED:
		    status = ManagedObjectStatus.WARNING;
	    }
	}

	return status;
    }

    public static String getStateString(SmfState state) {
	if (state == null) {
	    return null;
	}

	String lower = state.toString().toLowerCase();
	String key = "service.state." + lower;
	String text = Finder.getString(key);
	if (text == null) {
	    text = lower;
	}

	return text;
    }

    public static String getStateSynopsis(SmfState state, SmfState nextState) {
	String synopsis;

	if (state == null) {
	    synopsis = Finder.getString("service.state.synopsis.unknown");

	} else if (nextState == null || nextState == SmfState.NONE) {
	    String key =
		"service.state.synopsis." + state.toString().toLowerCase();

	    synopsis = Finder.getString(key);
	    if (synopsis == null) {
		key = "service.state.synopsis.default";
		synopsis = Finder.getString(key, state);
	    }
	} else {
	    String stateString = getStateString(state);
	    String nextStateString = getStateString(nextState);

	    synopsis = Finder.getString("service.state.synopsis.transitioning",
		stateString, nextStateString);
	}

	return synopsis;
    }

    public static String getStateDetails(
	SmfState state, SmfState nextState, String auxState, Date changed) {

	String details = null;

	if (state == null) {
	    details = Finder.getString("service.state.details.unknown");
	} else {
	    Calendar now = Calendar.getInstance();
	    Calendar then = Calendar.getInstance();
	    then.setTime(changed);

	    DateFormat formatter;

	    // Do these times refer to the same day?
	    if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
		now.get(Calendar.DAY_OF_YEAR) ==
		then.get(Calendar.DAY_OF_YEAR)) {
		formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
	    } else {
		formatter = DateFormat.getDateInstance(DateFormat.FULL);
	    }

	    String date = formatter.format(changed);

	    if (nextState == null || nextState == SmfState.NONE) {
		String key =
		    "service.state.details." + state.toString().toLowerCase();

		if (state == SmfState.MAINT) {
		    if (auxState != null) {
			key += "." + auxState.toLowerCase();
			details = Finder.getString(key, date);
		    }
		    if (details == null) {
			details = Finder.getString(
			    "service.state.details.maint.unknown", date);
		    }
		} else {
		    details = Finder.getString(key, date);
		    if (details == null) {
			key = "service.state.details.default";
			details = Finder.getString(key, state, date);
		    }
		}
	    } else {
		String stateString = getStateString(state);
		String nextStateString = getStateString(nextState);

		details = Finder.getString(
		    "service.state.details.transitioning",
		    stateString, nextStateString, date);
	    }
	}

	return details;
    }

    public static String getRecommendation(SmfState state) {
	String recommendation = null;

	if (state != null) {
	    String key = "service.state.recommendation." +
		state.toString().toLowerCase();

	    recommendation = Finder.getString(key);
	}

	return recommendation;
    }
}
