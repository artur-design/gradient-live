package com.example.gradient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.GridLayout;
import androidx.preference.CheckBoxPreference;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

	@Override
  	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
 		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new WallpaperSettingsFragment()).commit();
  	}

  	public static class WallpaperSettingsFragment extends PreferenceFragmentCompat {
  		int h, s, v, r, g, b;
		SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
  		@Override
  		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    		//super.onCreate(savedInstanceState);
			setPreferencesFromResource(R.xml.preferences,rootKey);
    		//addPreferencesFromResource(R.xml.preferences);
    		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editor = sharedPreferences.edit();
        // Получение частоты обновления экрана
	  		WindowManager windowManager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
	 	 	Display display = windowManager.getDefaultDisplay();
	  		final int refreshRate = (int) display.getRefreshRate();
    		ListPreference refreshRatePreference = (ListPreference) findPreference("screenRefreshRate");
      		refreshRatePreference.setTitle(getString(R.string.refresh_rate));
      		refreshRatePreference.setDialogTitle(getString(R.string.refresh_rate));

        // Генерация вариантов деленной частоты
	  		List<Integer> dividedRatesList = new ArrayList<>();
	  		for (int i = 1; i <= refreshRate; i++) {
		  		if (refreshRate % i == 0) dividedRatesList.add(refreshRate / i);
	  		}
    		String[] entries = new String[dividedRatesList.size()];
    		String[] values = new String[dividedRatesList.size()];
    		for (int i = 0; i < dividedRatesList.size(); i++) {
        		entries[i] = dividedRatesList.get(i) + getString(R.string.hz);
      			values[i] = String.valueOf(dividedRatesList.get(i));
    		}
  
    		refreshRatePreference.setEntries(entries);
    		refreshRatePreference.setEntryValues(values);
    		String savedValue = sharedPreferences.getString("screenRefreshRate", "60");
    		refreshRatePreference.setValue(savedValue);
      		refreshRatePreference.setSummary(getString(R.string.current_rate) + savedValue + getString(R.string.hz));
    		refreshRatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      			@Override
      			public boolean onPreferenceChange(Preference preference, Object newValue) {
        			int selectedValue = Integer.parseInt((String) newValue);
          			preference.setSummary(getString(R.string.current_rate) + selectedValue + getString(R.string.hz));
        			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        			editor.putString("screenRefreshRate", String.valueOf(selectedValue));
        			editor.apply();
        			return true;
      			}
    		});

    		EditTextPreference frameCountPreference = (EditTextPreference) findPreference("frameCount");
      		frameCountPreference.setSummary(getString(R.string.current_frames) + sharedPreferences.getString("frameCount", "100"));
    		frameCountPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      			@Override
      			public boolean onPreferenceChange(Preference preference, Object newValue) {
        			int frameCount;
        			try {
          				frameCount = Integer.parseInt((String) newValue);
        			} catch (NumberFormatException e) {
            			Toast.makeText(getActivity(), getString(R.string.err2), Toast.LENGTH_SHORT).show();
          				return false; // Не сохраняем новое значение
        			}
        			if (frameCount < 30) {
            			Toast.makeText(getActivity(), getString(R.string.err3), Toast.LENGTH_SHORT).show();
          				return false; // Не сохраняем новое значение
        			}
        			editor.putString("frameCount", String.valueOf (frameCount));
        			editor.apply();
					preference.setSummary(getString(R.string.current_frames) + frameCount);
        			return true; // Сохраняем новое значение
      			}
    		});

    		EditTextPreference gradientLengthCoefficientPreference = (EditTextPreference) findPreference("gradientLengthCoefficient");

    		//String savedCoefficient = sharedPreferences.getString("gradientLengthCoefficient", "1.0");
    		gradientLengthCoefficientPreference.setSummary(getString(R.string.current_coef) + sharedPreferences.getString("gradientLengthCoefficient", "1.0"));
    		gradientLengthCoefficientPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    			@Override
    			public boolean onPreferenceChange(Preference preference, Object newValue) {
      				float coefficient;
      				try {
        				coefficient = Float.parseFloat((String) newValue);
      				} catch (NumberFormatException e) {
          				Toast.makeText(getActivity(), getString(R.string.err2), Toast.LENGTH_SHORT).show();
        				return false;
      				}

      				if (coefficient < 1 || coefficient > 5) {
          				Toast.makeText(getActivity(), getString(R.string.err4), Toast.LENGTH_SHORT).show();
        				return false;
      				}

      				editor.putString("gradientLengthCoefficient", String.valueOf(coefficient));
      				editor.apply();
        			preference.setSummary(getString(R.string.current_coef) + coefficient);
      				return true;
      			}
    		});


    		Preference movementDirectionPref = findPreference("movementDirection");
      		movementDirectionPref.setSummary(getString(R.string.direction) + sharedPreferences.getString("movementDirection", "↘"));
    		movementDirectionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      			@Override
      			public boolean onPreferenceClick(Preference preference) {
        			showDirectionDialog();
        			return true;
      			}
    		});

    		CheckBoxPreference blackWallpaperPreference = (CheckBoxPreference) findPreference("blackWallpaper");
    		blackWallpaperPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      			@Override
      			public boolean onPreferenceChange(Preference preference, Object newValue) {
        			boolean isChecked = (Boolean) newValue;
        			editor.putBoolean("black", isChecked);
        			editor.apply();
        			return true;
      			}
    		});

CheckBoxPreference visibleWallpaperPreference = (CheckBoxPreference) findPreference("visibleMode");
    		visibleWallpaperPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      			@Override
      			public boolean onPreferenceChange(Preference preference, Object newValue) {
        			boolean isChecked = (Boolean) newValue;
        			editor.putBoolean("visibleMode", isChecked);
        			editor.apply();
        			return true;
      			}
    		});

    		Preference howToUsePreference = findPreference("howToUse");
    		howToUsePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      			@Override
      			public boolean onPreferenceClick(Preference preference) {
        			showHowToUseDialog();
        			return true;
      			}
    		});

Preference examplesPreference = findPreference("examples");
    		examplesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      			@Override
      			public boolean onPreferenceClick(Preference preference) {
        			showExamplesDialog();
        			return true;
      			}
    		});

			Preference colorGenerationRules = findPreference("colorGenerationRuless");
			colorGenerationRules.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
    			@Override
    			public boolean onPreferenceClick(Preference preference) {
        			showCustomDialog();
        			return true;
    			}
			});

ListPreference startGradient = (ListPreference) findPreference("startGradient");
            startGradient.setOnPreferenceChangeListener(
         new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object newValue) {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
             boolean isRandomStart = "start_random".equals(newValue);
 editor.putBoolean("startGradient", isRandomStart);
             editor.apply();
             return true;
             }
         });

    	}

		private void showDirectionDialog() {
    		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    		builder.setTitle(getString(R.string.select_direction));
    		LayoutInflater inflater = getActivity().getLayoutInflater();
    		View dialogView = inflater.inflate(R.layout.dialog_direction_buttons, null);
    		builder.setView(dialogView);
    		final GridLayout gridLayout = dialogView.findViewById(R.id.gridLayout);
			final AlertDialog dialog = builder.create();
			String savedDirection = sharedPreferences.getString("movementDirection", "↘");
    	// Устанавливаем обработчик нажатий для всех кнопок в GridLayout
    		for (int i = 0; i < gridLayout.getChildCount(); i++) {
      			View child = gridLayout.getChildAt(i);
      			if (child instanceof Button) {
        			final Button button = (Button) child;
int mintColor = ContextCompat.getColor(requireContext(), R.color.colorMint);
        		// Подсвечиваем кнопку, если ее текст совпадает с сохраненным направлением
        			if (button.getText().toString().equals(savedDirection)) button.setBackgroundColor(mintColor);
        			button.setOnClickListener(new View.OnClickListener() {
          				@Override
          				public void onClick(View v) {
			  				saveDirection(button.getText().toString()); // Извлекаем текст кнопки и сохраняем направление
            				dialog.dismiss();
          				}
        			});
      			}
    		}
    		dialog.show(); // Показываем диалог
		}
		
    	private void saveDirection(String direction) {
       		PreferenceManager.getDefaultSharedPreferences(getActivity())
      		.edit()
      		.putString("movementDirection", direction)
      		.apply();
        	((Preference) findPreference("movementDirection")).setSummary(getString(R.string.direction) + direction);
    	}


		public void showCustomDialog() {
    // Создаем экземпляр LayoutInflater
    		LayoutInflater inflater = getActivity().getLayoutInflater();
    		View dialogView = inflater.inflate(R.layout.preference_color_generation, null);
    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    		builder.setView(dialogView);
        // Получаем элементы из dialogView
    		RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
    		final RadioButton rgbRadioButton = dialogView.findViewById(R.id.rgbRadioButton);
    		RadioButton hsvRadioButton = dialogView.findViewById(R.id.hsvRadioButton);
    		final EditText param1 = dialogView.findViewById(R.id.param1);
    		final EditText param2 = dialogView.findViewById(R.id.param2);
    		final EditText param3 = dialogView.findViewById(R.id.param3);
    		final TextView label1 = dialogView.findViewById(R.id.label1);
    		final TextView label2 = dialogView.findViewById(R.id.label2);
    		final TextView label3 = dialogView.findViewById(R.id.label3);
    		String colorType = sharedPreferences.getString("myKey" + "_type", "HSV");
        	if (colorType.equals("RGB")) {
      			rgbRadioButton.setChecked(true);
				param1.setText(sharedPreferences.getString("myKey" + "_r", "random()"));
				param2.setText(sharedPreferences.getString("myKey" + "_g", "random()"));
				param3.setText(sharedPreferences.getString("myKey" + "_b", "random()")); 
        	} else {
            	hsvRadioButton.setChecked(true);
				param1.setText(sharedPreferences.getString("myKey" + "_h", "random()"));
				param2.setText(sharedPreferences.getString("myKey" + "_s", "random(150, 255)"));
				param3.setText(sharedPreferences.getString("myKey" + "_v", "random(150, 255)"));
				label1.setText("h=");
				label2.setText("s=");
				label3.setText("v=");
        	}
        	radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            	@Override
          		public void onCheckedChanged(RadioGroup group, int checkedId) {
   					if (checkedId == R.id.rgbRadioButton) {
  						param1.setText(sharedPreferences.getString("myKey" + "_r", "random()"));
						param2.setText(sharedPreferences.getString("myKey" + "_g", "random()"));
						param3.setText(sharedPreferences.getString("myKey" + "_b", "random()")); 
						label1.setText("r=");
						label2.setText("g=");
						label3.setText("b=");
   					} else if (checkedId == R.id.hsvRadioButton) {
						param1.setText(sharedPreferences.getString("myKey" + "_h", "random()"));
						param2.setText(sharedPreferences.getString("myKey" + "_s", "random(150, 255)"));
						param3.setText(sharedPreferences.getString("myKey" + "_v", "random(150, 255)"));
						label1.setText("h=");
						label2.setText("s=");
						label3.setText("v=");
                	}
            	}
        	});
    
    		builder.setTitle(getString(R.string.color_settings))
        	.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
                   // Обработка сохранения данных
      				onDialogClosed(true, param1, param2, param3, rgbRadioButton);
    			}
  			})
        	.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
      				dialog.dismiss();
    			}
  			})
        	.setNeutralButton(getString(R.string.def), new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
					param1.setText("random()");
					if (rgbRadioButton.isChecked()) {
						param2.setText("random()");
						param3.setText("random()");
    				} else {
						param2.setText("random(150, 255)");
						param3.setText("random(150, 255)");
					}
					onDialogClosed(true, param1, param2, param3, rgbRadioButton);
				}
  			});
  			final AlertDialog dialog = builder.create();
    		dialog.show();
		}

		private void onDialogClosed(boolean positiveResult, EditText param1, EditText param2, EditText param3, RadioButton rgbRadioButton) {
    		if (!positiveResult) return;
        // Код для сохранения данных
        	String[] rawParams = {param1.getText().toString(), param2.getText().toString(), param3.getText().toString() };
			int[] ints = {r, g, b, h, s, v};
			String[] keys = {"_r", "_g", "_b", "_h", "_s", "_v"};
			int temp;
        	try {
				temp = rgbRadioButton.isChecked() ? 0 : 3;
            // Проверка на ошибки и сохранение значений
				for (int i = 0; i < 3; i++) {
					if (isValidExpression(rawParams[i])) {
						ints[i + temp] = evaluateExpression(rawParams[i], rgbRadioButton);
						if (ints[i + temp] != -1) editor.putString("myKey" + keys[i + temp], rawParams[i]);
					} else ints[i + temp] = -1;
				}
				if(temp == 0) {
					r = ints[0]; g = ints[1]; b = ints[2];
 					editor.putString("myKey" + "_type", "RGB");
 				} else {
	 				h = ints[3]; s = ints[4]; v = ints[5];
 	 				editor.putString("myKey" + "_type", "HSV");
	 			}
     			String message = (temp == 0 ? "RGB: " : "HSV: ") + (ints[temp] != -1 ? String.valueOf(ints[temp]) : getString(R.string.error)) + ", " + (ints[1 + temp] != -1 ? String.valueOf(ints[1 + temp]) : getString(R.string.error)) + ", " + (ints[2 + temp] != -1 ? String.valueOf(ints[2 + temp]) : getString(R.string.error));
 	 			Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
 				editor.apply();
			} catch (IllegalArgumentException e) {
  				Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    		}
		}

		private int evaluateExpression(String expression, RadioButton rgbRadioButton) {
  			try {
    			expression = replaceRandomCalls(expression);
      			if (expression.matches(rgbRadioButton.isChecked() ? ".*[hsv].*" : ".*[rgb].*")) {
					Toast.makeText(getContext(), rgbRadioButton.isChecked() ? R.string.err5 : R.string.err6, Toast.LENGTH_SHORT).show();
					throw new IllegalArgumentException();
				}
    			Expression exp = new ExpressionBuilder(expression)
      			.variables("r", "g", "b", "h", "s", "v")
      			.build()
      			.setVariable("r", r)
      			.setVariable("g", g)
      			.setVariable("b", b)
      			.setVariable("h", h)
      			.setVariable("s", s)
      			.setVariable("v", v);

    			double result = exp.evaluate();
      int raw = (int) Math.round(result);
    			return ((raw % 256) + 256) % 256;
  			} catch (Exception e) {
    			e.printStackTrace();
    			return -1;
  			}
		}

    	private boolean isValidExpression(String expression) {
    		String regex = "^(h|s|v|r|g|b|random\\s*\\(\\s*\\)|random\\s*\\(\\s*(\\d+|h|s|v|r|g|b)\\s*\\)|random\\s*\\(\\s*(\\d+|h|s|v|r|g|b)\\s*,\\s*(\\d+|h|s|v|r|g|b)\\s*\\)|\\d+(\\.\\d+)?|\\+|\\-|/|\\*|%|\\(|\\)|\\s)*$";
      		return expression.matches(regex);
    	}

		private String replaceRandomCalls(String expression) {
    		Pattern pattern = Pattern.compile("random\\s*\\(\\s*((\\d+|[a-zA-Z_])\\s*,)?\\s*(\\d+|[a-zA-Z_])?\\s*\\)");
    		Matcher matcher = pattern.matcher(expression);
    		StringBuffer result = new StringBuffer();

    		while (matcher.find()) {
      			int min = 0;
      			int max = 255;
      			String minStr = matcher.group(2);
      			String maxStr = matcher.group(3);
      			if (minStr != null && !minStr.isEmpty()) {
        			min = getValue(minStr);
        			max = getValue(maxStr);
      			} else if (maxStr != null && !maxStr.isEmpty()) max = getValue(maxStr);
      			if (min > max) throw new IllegalArgumentException(getString(R.string.err1));
				matcher.appendReplacement(result, String.valueOf(min + Math.random() * (max - min + 1)));
    		}
    		matcher.appendTail(result);
    		return result.toString();
		}

		private int getValue(String input) {
    		switch (input) {
      			case "r": return r;
      			case "g": return g;
      			case "b": return b;
      			case "h": return h;
      			case "s": return s;
      			case "v": return v;
      			default: return Integer.parseInt(input);
    		}
		}


    	private void showHowToUseDialog() {
TextView tv = new TextView(getActivity());
    tv.setText(getString(R.string.about));
    tv.setTextSize(16);
    tv.setPadding(24, 24, 24, 24);
tv.setTextColor(Color.WHITE);
    tv.setAutoLinkMask(Linkify.WEB_URLS);
    tv.setMovementMethod(LinkMovementMethod.getInstance());
      		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        	builder.setTitle(getString(R.string.how_use))
            .setView(tv)
			.setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
         		public void onClick(DialogInterface dialog, int id) {
           			dialog.dismiss();
         		}
       		});
      		AlertDialog dialog = builder.create();
      		dialog.show();
    	}

private void showExamplesDialog() {
                      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.some_examples))
            .setMessage(getString(R.string.list_example))
                        .setPositiveButton(getString(R.string.ok),new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                                   dialog.dismiss();
                         }
                       });
                      AlertDialog dialog = builder.create();
                      dialog.show();
            }
  	}
}
