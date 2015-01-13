/*
 * Copyright (C) 2014 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.affirmations;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.pgp.exception.PgpKeyNotFoundException;
import org.sufficientlysecure.keychain.provider.CachedPublicKeyRing;
import org.sufficientlysecure.keychain.provider.ProviderHelper;
import org.sufficientlysecure.keychain.util.Log;

public class AffirmationWizard extends ActionBarActivity {

    public static final int FRAG_ACTION_START = 0;
    public static final int FRAG_ACTION_TO_RIGHT = 1;
    public static final int FRAG_ACTION_TO_LEFT = 2;

    long mMasterKeyId;
    byte[] mFingerprint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_key_activity);

        try {
            Uri uri = getIntent().getData();
            CachedPublicKeyRing ring = new ProviderHelper(this).getCachedPublicKeyRing(uri);
            mMasterKeyId = ring.extractOrGetMasterKeyId();
            mFingerprint = ring.getFingerprint();
        } catch (PgpKeyNotFoundException e) {
            Log.e(Constants.TAG, "Invalid uri given, key does not exist!");
            finish();
            return;
        }

        // pass extras into fragment
        AffirmationSelectFragment frag = AffirmationSelectFragment.newInstance();
        loadFragment(null, frag, FRAG_ACTION_START);
    }

    public void loadFragment(Bundle savedInstanceState, Fragment fragment, int action) {
        // However, if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            return;
        }

        // Add the fragment to the 'fragment_container' FrameLayout
        // NOTE: We use commitAllowingStateLoss() to prevent weird crashes!
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (action) {
            case FRAG_ACTION_START:
                transaction.setCustomAnimations(0, 0);
                transaction.replace(R.id.create_key_fragment_container, fragment)
                        .commitAllowingStateLoss();
                break;
            case FRAG_ACTION_TO_LEFT:
                getSupportFragmentManager().popBackStackImmediate();
                break;
            case FRAG_ACTION_TO_RIGHT:
                transaction.setCustomAnimations(R.anim.frag_slide_in_from_right, R.anim.frag_slide_out_to_left,
                        R.anim.frag_slide_in_from_left, R.anim.frag_slide_out_to_right);
                transaction.addToBackStack(null);
                transaction.replace(R.id.create_key_fragment_container, fragment)
                        .commitAllowingStateLoss();
                break;

        }
        // do it immediately!
        getSupportFragmentManager().executePendingTransactions();
    }

}