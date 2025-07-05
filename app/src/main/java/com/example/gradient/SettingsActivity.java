package com.example.gradient;


import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.GridLayout;
import android.preference.CheckBoxPreference;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.view.WindowManager;
import android.view.Display;
import android.graphics.Color;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class SettingsActivity extends Activity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
 getFragmentManager().beginTransaction().replace(android.R.id.content, new WallpaperSettingsFragment()).commit();
  }

  public static class WallpaperSettingsFragment extends PreferenceFragment {
  int h, s, v, r, g, b;
SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editor = sharedPreferences.edit();
        // Получение частоты обновления экрана
    final int refreshRate = (int) getScreenRefreshRate();
    ListPreference refreshRatePreference = (ListPreference) findPreference("screenRefreshRate");
    refreshRatePreference.setTitle("Частота обновления");
    refreshRatePreference.setDialogTitle("Частота обновления");

        // Генерация вариантов деленной частоты
    int[] dividedRates = generateDividedRates(refreshRate);
    String[] entries = new String[dividedRates.length];
    String[] values = new String[dividedRates.length];
    for (int i = 0; i < dividedRates.length; i++) {
      entries[i] = dividedRates[i] + " Гц";
      values[i] = String.valueOf(dividedRates[i]);
    }

    refreshRatePreference.setEntries(entries);
    refreshRatePreference.setEntryValues(values);
    String savedValue = sharedPreferences.getString("screenRefreshRate", "60");
    refreshRatePreference.setValue(savedValue);
    refreshRatePreference.setSummary("Текущая частота: " + savedValue + " Гц");
    refreshRatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        int selectedValue = Integer.parseInt((String) newValue);
        preference.setSummary("Текущая частота: " + selectedValue + " Гц");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString("screenRefreshRate", String.valueOf(selectedValue));
        editor.apply();
        return true;
      }
    });

    EditTextPreference frameCountPreference = (EditTextPreference) findPreference("frameCount");
        
    String savedFrameCount = sharedPreferences.getString("frameCount", "100");
    frameCountPreference.setSummary("Текущее количество кадров: " + savedFrameCount);

    frameCountPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        int frameCount;
        try {
          frameCount = Integer.parseInt((String) newValue);
        } catch (NumberFormatException e) {
          Toast.makeText(getActivity(), "Введите корректное число", Toast.LENGTH_SHORT).show();
          return false;
        }

        if (frameCount < 30) {
          Toast.makeText(getActivity(), "Введите значение от 30", Toast.LENGTH_SHORT).show();
          return false;
        }
        
        preference.setSummary("Текущее количество кадров: " + frameCount);
        editor.putString("frameCount", String.valueOf (frameCount));
        editor.apply();
        return true; // Сохраняем новое значение
      }
    });

    EditTextPreference gradientLengthCoefficientPreference = (EditTextPreference) findPreference("gradientLengthCoefficient");

    String savedCoefficient = sharedPreferences.getString("gradientLengthCoefficient", "1.0");
    gradientLengthCoefficientPreference.setSummary("Текущий коэффициент: " + savedCoefficient);
    gradientLengthCoefficientPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      float coefficient;
      try {
        coefficient = Float.parseFloat((String) newValue);
      } catch (NumberFormatException e) {
        Toast.makeText(getActivity(), "Введите корректное число", Toast.LENGTH_SHORT).show();
        return false; // Не сохраняем новое значение
      }

      if (coefficient < 1 || coefficient > 5) {
        Toast.makeText(getActivity(), "Введите значение от 1 до 5", Toast.LENGTH_SHORT).show();
        return false; // Не сохраняем новое значение
      }

      editor.putString("gradientLengthCoefficient", String.valueOf(coefficient));
      editor.apply();
      preference.setSummary("Текущий коэффициент: " + coefficient);
      return true; // Сохраняем новое значение
      }
    });


    Preference movementDirectionPref = findPreference("movementDirection");
      String savedDirection = sharedPreferences.getString("movementDirection", "↘");
      movementDirectionPref.setSummary("Направление: " + savedDirection);

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

    Preference howToUsePreference = findPreference("howToUse");
    howToUsePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        showHowToUseDialog();
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
        
    }

    private float getScreenRefreshRate() {
        WindowManager windowManager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        return display.getRefreshRate(); // Возвращает частоту обновления экрана
    }

    private int[] generateDividedRates(int refreshRate) {
        // Создаем список деленных значений
        List<Integer> dividedRatesList = new ArrayList<>();
        // Генерация деленных значений от 1 до refreshRate
        for (int i = 1; i <= refreshRate; i++) {
            if (refreshRate % i == 0) { // Проверяем, является ли частное целым
                dividedRatesList.add(refreshRate / i);
            }
        }

        // Преобразуем список в массив
        int[] dividedRates = new int[dividedRatesList.size()];
        for (int i = 0; i < dividedRatesList.size(); i++) {
            dividedRates[i] = dividedRatesList.get(i);
        }

        return dividedRates;
    }

    

private void showDirectionDialog() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Выберите направление");

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

        // Подсвечиваем кнопку, если ее текст совпадает с сохраненным направлением
        if (button.getText().toString().equals(savedDirection)) {
button.setBackgroundColor(Color.parseColor("#00cea8"));
        }

        button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            // Извлекаем текст кнопки и сохраняем направление
            String direction = button.getText().toString();
            saveDirection(direction);
            dialog.dismiss();
          }
        });
      }
    }

    // Показываем диалог
    dialog.show();
}

    private void saveDirection(String direction) {
       PreferenceManager.getDefaultSharedPreferences(getActivity())
      .edit()
      .putString("movementDirection", direction)
      .apply();
      ((Preference) findPreference("movementDirection")).setSummary("Направление: " + direction);
    }


public void showCustomDialog() {
    // Создаем экземпляр LayoutInflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.preference_color_generation, null);
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setView(dialogView);
    // Инициализация элементов интерфейса из вашего макета
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
    

  builder.setTitle("Настройки цвета")
  .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
                   // Обработка сохранения данных
      onDialogClosed(true, param1, param2, param3, rgbRadioButton);
    }
  })
  .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      dialog.dismiss();
    }
  })
  .setNeutralButton("По умолчанию", new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
      //setDefaultValues(param1, param2, param3);
if (rgbRadioButton.isChecked()) {
param1.setText("random()");
param2.setText("random()");
param3.setText("random()");
    } else {
param1.setText("random()");
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
    if (positiveResult) {
        // Ваш код для сохранения данных
        String rawParam1 = param1.getText().toString();
        String rawParam2 = param2.getText().toString();
        String rawParam3 = param3.getText().toString();

        try {
            if (rgbRadioButton.isChecked()) {
                // Проверка на ошибки и сохранение значений
 if (isValidExpression(rawParam1)) {
   r = evaluateExpression(rawParam1, rgbRadioButton);
                if (r != -1) {
                    editor.putString("myKey" + "_r", rawParam1);
                }
} else r = -1;
 if (isValidExpression(rawParam2)) {
   g = evaluateExpression(rawParam2, rgbRadioButton);
   if (g != -1) {
     editor.putString("myKey" + "_g", rawParam2);
   }
 } else g = -1;
 if (isValidExpression(rawParam3)) {
   b = evaluateExpression(rawParam3, rgbRadioButton);
   if (b != -1) {
     editor.putString("myKey" + "_b", rawParam3);
   }
 } else b = -1;
 editor.putString("myKey" + "_type", "RGB");
 String message = "RGB: " + (r != -1 ? r : "Ошибка") + ", " +     (g != -1 ? g : "Ошибка") + ", " + (b != -1 ? b : "Ошибка");
 Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
 } else {
 if (isValidExpression(rawParam1)) {
   h = evaluateExpression(rawParam1, rgbRadioButton);
   if (h != -1) {
     editor.putString("myKey" + "_h", rawParam1);
   }
 } else h = -1;

 if (isValidExpression(rawParam2)) {
   s = evaluateExpression(rawParam2, rgbRadioButton);
   if (s != -1) {
     editor.putString("myKey" + "_s", rawParam2);
   }
 } else s = -1;

 if (isValidExpression(rawParam3)) {
   v = evaluateExpression(rawParam3, rgbRadioButton);
   if (v != -1) {
     editor.putString("myKey" + "_v", rawParam3);
   }
 } else v = -1;

 editor.putString("myKey" + "_type", "HSV");

 String message = "HSV: " + (h != -1 ? h : "Ошибка") + ", " + (s != -1 ? s : "Ошибка") + ", " + (v != -1 ? v : "Ошибка");
 Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
 }
 editor.apply();
} catch (IllegalArgumentException e) {
  Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }
}

private int evaluateExpression(String expression, RadioButton rgbRadioButton) {
  try {
    expression = replaceRandomCalls(expression);
    if (rgbRadioButton.isChecked()) {
      if (expression.matches(".*[hsv].*")) {
        throw new IllegalArgumentException("Использование переменных h, s, v не разрешено в режиме RGB.");
      }
    } else {
      if (expression.matches(".*[rgb].*")) {
        throw new IllegalArgumentException("Использование переменных r, g, b не разрешено в режиме HSV.");
      }
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
    int intResult = (int) result % 256;
    return Math.max(intResult, 0);
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
      } else if (maxStr != null && !maxStr.isEmpty()) {

max = getValue(maxStr);
          
      }
      if (min > max) {
        throw new IllegalArgumentException("Минимальное значение не может быть больше максимального.");
      }

      String randomValue = String.valueOf(getRandomValue(min, max));
      matcher.appendReplacement(result, randomValue);
    }
    matcher.appendTail(result);
    return result.toString();
}

private int getValue(String input) {
    switch (input) {
      case "r":
        return r;
      case "g":
        return g;
      case "b":
        return b;
      case "h":
        return h;
      case "s":
        return s;
      case "v":
        return v;
      default:
        return Integer.parseInt(input);
    }
}

  private int getRandomValue(int min, int max) {
    return (int) (min + (Math.random() * (max - min + 1)));
  }

    private void showHowToUseDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle("Как пользоваться")
        .setMessage("gradient live - это живые градиентные обои. В приложении 'gradient live' доступны следующие настройки:\n\n1. Частота обновления. Здесь вы можете выбрать частоту кадров, максимальным количеством которой является частота экрана вашего телефона.\n2. Количество кадров. Здесь вы можете ввеси количество кадров градиента от 30, что влияет на скорость и плавность движения.\n3. Коэффициент длины градиента. Здесь вы можете выбрать длину градиента в экранах, вводя дробные или целое число от 1 до 5,что влияет на скорость и резкость градиента.\n4. Направление движения. В этом пункте вы можете выбрать направление движения градиента. Если же вы выберете центр, то градиента не будет, но весь экран будет одного цвета, который будет плавно меняться а соответствии с вашими настройками.\n5. в правилах генерации цвета вы устанавливаете закономерность генерации следующего цвета. Там доступны переменные: r, g, b, h, s, v, random(), random(max), random(min, max); числа, в том числе и дробные; операторы: '+', '-', '*', '/', '%'; а также скобки '(' и ')'. Если введенное выражение содержит что-то, кроме этого, или не сможет выполниться даже в первый раз, то оно не будет сохранено. Если же оно не сможет выполниться в последующем, то функция вернёт значение 0")
           .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int id) {
           dialog.dismiss();
         }
       });

      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }
}
