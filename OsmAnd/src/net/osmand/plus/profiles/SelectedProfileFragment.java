package net.osmand.plus.profiles;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import net.osmand.PlatformUtil;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.OsmandActionBarActivity;
import net.osmand.plus.activities.SettingsNavigationActivity;
import net.osmand.plus.base.BaseOsmAndFragment;
import net.osmand.plus.profiles.ProfileBottomSheetDialogFragment.ProfileTypeDialogListener;
import net.osmand.plus.profiles.SelectIconBottomSheetDialogFragment.IconIdListener;
import net.osmand.plus.widgets.OsmandTextFieldBoxes;
import net.osmand.router.GeneralRouter.GeneralRouterProfile;
import org.apache.commons.logging.Log;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class SelectedProfileFragment extends BaseOsmAndFragment {

	private static final Log LOG = PlatformUtil.getLog(SelectedProfileFragment.class);

	public static final String OPEN_CONFIG_ON_MAP = "openConfigOnMap";
	public static final String MAP_CONFIG = "openMapConfigMenu";
	public static final String NAV_CONFIG = "openNavConfigMenu";
	public static final String SCREEN_CONFIG = "openScreenConfigMenu";
	public static final String SELECTED_PROFILE = "editedProfile";



	ApplicationMode profile = null;
	ApplicationMode parent = null;
	ArrayList<RoutingProfile> routingProfiles;
	OsmandApplication app;
	RoutingProfile selectedRoutingProfile = null;
	float defSpeed = 0f;

	private boolean isNew = false;
	private boolean isDataChanged = false;

	private ProfileTypeDialogListener profileTypeDialogListener = null;
	private IconIdListener iconIdListener = null;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = getMyApplication();
		if (getArguments() != null) {
			String modeName = getArguments().getString("stringKey", "car");
			if (!getArguments().getBoolean("isNew", true)) {
				profile = ApplicationMode.valueOfStringKey(modeName, ApplicationMode.CAR);
			} else {
				isNew = true;
				parent = ApplicationMode.valueOfStringKey(modeName, ApplicationMode.CAR);
			}
		}
		routingProfiles = getRoutingProfiles();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		final boolean isNightMode = !app.getSettings().isLightContent();

		View view = inflater.inflate(R.layout.fragment_selected_profile, container, false);

		final ImageView profileIcon = view.findViewById(R.id.select_icon_btn_img);
		final LinearLayout profileIconBtn = view.findViewById(R.id.profile_icon_layout);
		final ExtendedEditText profileNameEt = view.findViewById(R.id.profile_name_et);
		final OsmandTextFieldBoxes profileNameTextBox = view.findViewById(R.id.profile_name_otfb);
		final ExtendedEditText navTypeEt = view.findViewById(R.id.navigation_type_et);
		final OsmandTextFieldBoxes navTypeTextBox = view.findViewById(R.id.navigation_type_otfb);
		final FrameLayout selectNavTypeBtn = view.findViewById(R.id.select_nav_type_btn);
		final Button cancelBtn = view.findViewById(R.id.cancel_button);
		final Button saveButton = view.findViewById(R.id.save_profile_btn);
		final View mapConfigBtn = view.findViewById(R.id.map_config_btn);
		final View screenConfigBtn = view.findViewById(R.id.screen_config_btn);
		final View navConfigBtn = view.findViewById(R.id.nav_settings_btn);

		profileIconBtn.setBackgroundResource(R.drawable.rounded_background_3dp);
		GradientDrawable selectIconBtnBackground = (GradientDrawable) profileIconBtn
			.getBackground();
		if(isNew) {
			profileIcon.setImageDrawable(app.getUIUtilities().getIcon(parent.getSmallIconDark(),
				isNightMode ? R.color.active_buttons_and_links_dark
					: R.color.active_buttons_and_links_light));
		} else {
			profileIcon.setImageDrawable(app.getUIUtilities().getIcon(profile.getSmallIconDark(),
				isNightMode ? R.color.active_buttons_and_links_dark
					: R.color.active_buttons_and_links_light));
		}

		if (isNightMode) {
			profileNameTextBox
				.setPrimaryColor(ContextCompat.getColor(app, R.color.color_dialog_buttons_dark));
			navTypeTextBox
				.setPrimaryColor(ContextCompat.getColor(app, R.color.color_dialog_buttons_dark));
			selectIconBtnBackground
				.setColor(app.getResources().getColor(R.color.text_field_box_dark));
		} else {
			selectIconBtnBackground
				.setColor(app.getResources().getColor(R.color.text_field_box_light));
		}

		profileTypeDialogListener = new ProfileTypeDialogListener() {
			@Override
			public void onSelectedType(int pos) {
				selectedRoutingProfile = routingProfiles.get(pos);
				navTypeEt.setText(selectedRoutingProfile.getName());
			}
		};

		iconIdListener = new IconIdListener() {
			@Override
			public void selectedIconId(int iconRes) {
				profile.setMapIconId(iconRes);
				profile.setSmallIconDark(iconRes);
				profileIcon.setImageDrawable(app.getUIUtilities().getIcon(iconRes,
					isNightMode ? R.color.active_buttons_and_links_dark
						: R.color.active_buttons_and_links_light));
			}
		};

		profileNameEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (getActivity() instanceof OsmandActionBarActivity) {
					ActionBar actionBar = ((OsmandActionBarActivity) getActivity()).getSupportActionBar();
					if (actionBar != null) {
						actionBar.setTitle(s.toString());
					}
				}
			}
		});

		selectNavTypeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final ProfileBottomSheetDialogFragment fragment = new ProfileBottomSheetDialogFragment();
				fragment.setProfileTypeListener(profileTypeDialogListener);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("routing_profiles", routingProfiles);
				fragment.setArguments(bundle);
				if (getActivity() != null) {
					getActivity().getSupportFragmentManager().beginTransaction()
						.add(fragment, "select_nav_type")
						.commitAllowingStateLoss();
				}


//				navTypeEt.setText("Car");
//				navTypeEt.setCursorVisible(false);
//				navTypeEt.setTextIsSelectable(false);
//				navTypeEt.clearFocus();
				navTypeEt.requestFocus(ExtendedEditText.FOCUS_UP);
//				LOG.debug("click on text");

			}
		});

		profileIconBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final SelectIconBottomSheetDialogFragment iconSelectDialog = new SelectIconBottomSheetDialogFragment();
				iconSelectDialog.setIconIdListener(iconIdListener);
				Bundle bundle = new Bundle();
				bundle.putInt("selectedIcon", profile.getSmallIconDark());
				iconSelectDialog.setArguments(bundle);
				if (getActivity() != null) {
					getActivity().getSupportFragmentManager().beginTransaction()
						.add(iconSelectDialog, "select_icon")
						.commitAllowingStateLoss();
				}

			}
		});

		//todo switch app to edited mode on activity start
		mapConfigBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isDataChanged) {
					//todo save before living
				} else {
					Intent i = new Intent(getActivity(), MapActivity.class);
					i.putExtra(OPEN_CONFIG_ON_MAP, MAP_CONFIG);
					i.putExtra(SELECTED_PROFILE, profile.getStringKey());
					startActivity(i);
				}
			}
		});

		screenConfigBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isDataChanged) {
					//todo save before living
				} else {
					Intent i = new Intent(getActivity(), MapActivity.class);
					i.putExtra(OPEN_CONFIG_ON_MAP, SCREEN_CONFIG);
					i.putExtra(SELECTED_PROFILE, profile.getStringKey());
					startActivity(i);
				}
			}
		});

		//todo edited mode on activity start
		navConfigBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isDataChanged) {
					//todo save before living
				} else {
					Intent i = new Intent(getActivity(), SettingsNavigationActivity.class);
					i.putExtra(OPEN_CONFIG_ON_MAP, NAV_CONFIG);
					i.putExtra(SELECTED_PROFILE, profile.getStringKey());
					startActivity(i);
				}
			}
		});

		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() != null) {
					getActivity().onBackPressed();
				}
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = profileNameTextBox.getEditText().getText().toString();
				if (ApplicationMode.allPossibleValues().contains(profile)) {
					updateProfile();
				} else {
					saveNewProfile(title, parent, selectedRoutingProfile, defSpeed);
				}
			}
		});

		return view;
	}

	private boolean saveNewProfile(
		String userProfileTitle,
		ApplicationMode profile,
		RoutingProfile selectedRoutingProfile,
		float defSpeed) {


		String customStringKey = profile.getParent().getStringKey() + "_" + userProfileTitle.hashCode();
		for (ApplicationMode mode : ApplicationMode.allPossibleValues()) {
			if (mode.getStringKey().equals(customStringKey)) {
				//todo notify user that there is already profile with such name
				return false;
			}
		}


		ApplicationMode.createCustomMode(userProfileTitle, customStringKey);
		//todo build profile, save and register:

		return true;
	}

	private void updateProfile() {

	}

	/**
	 * For now there are only default nav profiles todo: add profiles from custom routing xml-s
	 */
	private ArrayList<RoutingProfile> getRoutingProfiles() {
		ArrayList<RoutingProfile> routingProfiles = new ArrayList<>();
		for (GeneralRouterProfile navProfileName : GeneralRouterProfile.values()) {
			String name = "";
			int iconRes = -1;
			switch (navProfileName) {
				case CAR:
					iconRes = R.drawable.ic_action_car_dark;
					name = getString(R.string.rendering_value_car_name);
					break;
				case PEDESTRIAN:
					iconRes = R.drawable.map_action_pedestrian_dark;
					name = getString(R.string.rendering_value_pedestrian_name);
					break;
				case BICYCLE:
					iconRes = R.drawable.map_action_bicycle_dark;
					name = getString(R.string.rendering_value_bicycle_name);
					break;
				case PUBLIC_TRANSPORT:
					iconRes = R.drawable.map_action_bus_dark;
					name = getString(R.string.app_mode_public_transport);
					break;
				case BOAT:
					iconRes = R.drawable.map_action_sail_boat_dark;
					name = getString(R.string.app_mode_boat);
					break;
			}
			routingProfiles
				.add(new RoutingProfile(name, getResources().getString(R.string.osmand_default_routing), iconRes, false));
		}
		return routingProfiles;
	}
}
