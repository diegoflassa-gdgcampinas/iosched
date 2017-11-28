/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg_campinas.treffen.debug;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.gdg_campinas.treffen.debug.actions.DisplayUserDataDebugAction;
import org.gdg_campinas.treffen.debug.actions.ForceAppDataSyncNowAction;
import org.gdg_campinas.treffen.debug.actions.ForceSyncNowAction;
import org.gdg_campinas.treffen.debug.actions.ScheduleStarredSessionAlarmsAction;
import org.gdg_campinas.treffen.debug.actions.ShowSessionNotificationDebugAction;
import org.gdg_campinas.treffen.debug.actions.TestScheduleHelperAction;
import org.gdg_campinas.treffen.info.BaseInfoFragment;
import org.gdg_campinas.treffen.lib.BuildConfig;
import org.gdg_campinas.treffen.schedule.ScheduleActivity;
import org.gdg_campinas.treffen.service.SessionAlarmService;
import org.gdg_campinas.treffen.settings.ConfMessageCardUtils;
import org.gdg_campinas.treffen.settings.SettingsUtils;
import org.gdg_campinas.treffen.util.AccountUtils;
import org.gdg_campinas.treffen.util.LogUtils;
import org.gdg_campinas.treffen.util.RegistrationUtils;
import org.gdg_campinas.treffen.util.TimeUtils;
import org.gdg_campinas.treffen.util.WiFiUtils;
import org.gdg_campinas.treffen.welcome.WelcomeActivity;

/**
 * {@link android.app.Activity} displaying debug options so a developer can debug and test. This
 * functionality is only enabled when {@link BuildConfig}.DEBUG
 * is true.
 */
public class DebugFragment extends BaseInfoFragment {

    private static final String TAG = LogUtils.makeLogTag(DebugFragment.class);

    /**
     * Area of screen used to display log log messages.
     */
    private TextView mLogArea;
    private ViewGroup mTestActionsList;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(org.gdg_campinas.treffen.lib.R.layout.debug_frag, container, false);
        mLogArea = (TextView) rootView.findViewById(org.gdg_campinas.treffen.lib.R.id.logArea);
        mTestActionsList = (ViewGroup) rootView.findViewById(org.gdg_campinas.treffen.lib.R.id.debug_action_list);

        createTestAction(new ForceSyncNowAction());
        createTestAction(new DisplayUserDataDebugAction());
        createTestAction(new ForceAppDataSyncNowAction());
        createTestAction(new TestScheduleHelperAction());
        createTestAction(new ScheduleStarredSessionAlarmsAction());
        createTestAction(new DebugAction() {
            @Override
            public void run(final Context context, final Callback callback) {
                final String sessionId = SessionAlarmService.DEBUG_SESSION_ID;
                final String sessionTitle = "Debugging with Placeholder Text";

                Intent intent = new Intent(
                        SessionAlarmService.ACTION_NOTIFY_SESSION_FEEDBACK,
                        null, context, SessionAlarmService.class);
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_ID, sessionId);
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_START, System.currentTimeMillis()
                        - 30 * 60 * 1000);
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_END, System.currentTimeMillis());
                intent.putExtra(SessionAlarmService.EXTRA_SESSION_TITLE, sessionTitle);
                context.startService(intent);
                Toast.makeText(context, "Showing DEBUG session feedback notification.",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public String getLabel() {
                return "Show session feedback notification";
            }
        });
        createTestAction(new ShowSessionNotificationDebugAction());
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                RegistrationUtils.setRegisteredAttendee(context, false);
                AccountUtils.setActiveAccount(context, null);
                context.startActivity(new Intent(context, WelcomeActivity.class));
            }

            @Override
            public String getLabel() {
                return "Display Welcome Activity";
            }
        });

        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                RegistrationUtils.setRegisteredAttendee(context, false);
                AccountUtils.setActiveAccount(context, null);
                ConfMessageCardUtils.unsetStateForAllCards(context);
            }

            @Override
            public String getLabel() {
                return "Reset Welcome Flags";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                Intent intent = new Intent(context, ScheduleActivity.class);
                intent.putExtra(ScheduleActivity.EXTRA_FILTER_TAG, "TRACK_ANDROID");
                context.startActivity(intent);
            }

            @Override
            public String getLabel() {
                return "Show filtered Schedule (Android Topic)";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                LogUtils.LOGW(TAG, "Unsetting all Explore I/O message card answers.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(context, null);
                ConfMessageCardUtils.setConfMessageCardsEnabled(context, null);
                ConfMessageCardUtils.unsetStateForAllCards(context);
            }

            @Override
            public String getLabel() {
                return "Unset all My I/O-based card answers";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfConference(context, -TimeUtils.HOUR * 3);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours before Conf";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfConference(context, -TimeUtils.DAY);
            }

            @Override
            public String getLabel() {
                return "Set time to day before Conf";
            }
        });

        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfConference(context, TimeUtils.HOUR * 3);

                LogUtils.LOGW(TAG, "Unsetting all Explore I/O card answers and settings.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(context, null);
                ConfMessageCardUtils.setConfMessageCardsEnabled(context, null);
                SettingsUtils.markDeclinedWifiSetup(context, false);
                WiFiUtils.uninstallConferenceWiFi(context);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours after Conf start";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToStartOfSecondDayOfConference(context,
                        TimeUtils.HOUR * 3);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours after 2nd day start";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.setCurrentTimeRelativeToEndOfConference(context, TimeUtils.HOUR * 3);
            }

            @Override
            public String getLabel() {
                return "Set time to 3 hours after Conf end";
            }
        });
        createTestAction(new DebugAction() {
            @Override
            public void run(Context context, Callback callback) {
                TimeUtils.clearMockCurrentTime(context);
            }

            @Override
            public String getLabel() {
                return "Clear mock current time";
            }
        });

        return rootView;
    }

    protected void createTestAction(final DebugAction test) {
        Button testButton = new Button(mTestActionsList.getContext());
        testButton.setText(test.getLabel());
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long start = System.currentTimeMillis();
                mLogArea.setText("");
                test.run(view.getContext(), new DebugAction.Callback() {
                    @Override
                    public void done(boolean success, String message) {
                        logTimed((System.currentTimeMillis() - start),
                                (success ? "[OK] " : "[FAIL] ") + message);
                    }
                });
            }
        });
        mTestActionsList.addView(testButton);
    }

    protected void logTimed(long time, String message) {
        message = "[" + time + "ms] " + message;
        Log.d(TAG, message);
        mLogArea.append(message + "\n");
    }

    @Override
    public String getTitle(Resources resources) {
        return "DEBUG";
    }

    @Override
    public void updateInfo(Object info) {
        //
    }

    @Override
    protected void showInfo() {
        //
    }
}
