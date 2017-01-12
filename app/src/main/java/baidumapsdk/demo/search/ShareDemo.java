package baidumapsdk.demo.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import baidumapsdk.demo.R;

public class ShareDemo extends Activity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_demo);
        editText = (EditText) findViewById(R.id.et_num);
    }

    /*int类型的正数数据的位数集合*/
    private int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    private String[] chineseInts = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private String[] chineseUnit1 = {"十", "百", "千"};
    private String[] chineseUnit2 = {"万", "亿"};

    /*该方法用于判断int类型数据的位数*/
    private int sizeOfInt(int x) {
        int size = -1;
        w:
        for (int i = 0; i < sizeTable.length; i++) {
            if (x <= sizeTable[i]) {
                size = i + 1;
                break w;
            }
        }
        return size;
    }

    /*
    * multiple:代表有几个10相乘
    * return:返回的是1代表multiple的值小1
    * */
    private int getTenMultiple(int multiple) {
        int tenMultiple = 1;
        if (multiple >= 1) {
            tenMultiple = 10;
            for (int i = 1; i < multiple; i++) {
                tenMultiple = tenMultiple * 10;
            }
        }
        return tenMultiple;
    }

    /*将analysisInt参数,各个位的int数字分割出来,analysisInt在方法中不断减少,正确执行的话analysisInt为0*/
    private int[] getIntEachValue(int analysisInt) {
        int sizeOfInt = sizeOfInt(analysisInt);
        int[] intEachValue = new int[sizeOfInt];
        for (int i = 0; i < sizeOfInt; i++) {
            intEachValue[i] = analysisInt / getTenMultiple(sizeOfInt - 1 - i);
            analysisInt = analysisInt - intEachValue[i] * getTenMultiple(sizeOfInt - 1 - i);
        }
        if (analysisInt == 0) {
            return intEachValue;
        } else {
            return null;
        }
    }

    /*根据intEachValue参数将其中的四位数转换为中文读法*/
    private String getChineseInt(int[] intEachValue) {
        int addZeroSome = 0;
        String chineseInt = "";
        for (int i = 0; i < intEachValue.length; i++) {
            String value = chineseInts[intEachValue[i]];
            String unit = "";
            if ((intEachValue.length - 2 - i) >= 0) {
                unit = chineseUnit1[intEachValue.length - 2 - i];
            }

            if (value.contains("零")) {
                if (addZeroSome == 0) {
                    chineseInt = chineseInt + value;
                }
                addZeroSome++;
            } else {
                addZeroSome = 0;
                chineseInt = chineseInt + value + unit;
            }
        }
        String lastOneChar = chineseInt.substring(chineseInt.length() - 1, chineseInt.length());
        if (lastOneChar.contains("零")) {
            chineseInt = chineseInt.substring(0, chineseInt.length() - 1);
        }
        return chineseInt;
    }

    public void startShareDemo(View view) {
        Intent intent = new Intent();
        intent.setClass(this, ShareDemoActivity.class);
        startActivity(intent);
        /*String s = editText.getText().toString().trim();
        if (isEmpty(s)) {
            return;
        }

        int page1 = Integer.parseInt(s);

        int[] intEachValue = getIntEachValue(page1);
        getChineseInt(intEachValue);

        int page2 = page1 + 1;

        int[] intEachValue1 = getIntEachValue(page2);*/

        /*new Thread() {
            @Override
            public void run() {
                super.run();
                boolean isReadWriter = false;
                BufferedReader bufferedReader = null;
                BufferedWriter bufferedWriter = null;
                try {
                    File file = new File(Environment.getExternalStorageDirectory() + "/text.txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("text1.txt")));
                    bufferedWriter = new BufferedWriter(new FileWriter(file));
                    String readLine;
                    while ((readLine = bufferedReader.readLine()) != null) {
                        if (readLine.contains(getPage(bai1, shi1, ge1))) {// getPage(bai1, shi1, ge1)
                            isReadWriter = true;
                        } else if (readLine.contains(getPage(bai2, shi2, ge2))) {// getPage(bai2, shi2, ge2)
                            isReadWriter = false;
                        }
                        if (isReadWriter) {
                            Log.v("WS", readLine);
                            System.out.println("Instant Run Runtime started. Android package is baidumapsdk.demo, real application class is baidumapsdk.demo.");
//                            bufferedWriter.write(readLine);
//                            bufferedWriter.newLine();
//                            bufferedWriter.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bufferedWriter.close();
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();*/


    }

    public boolean isEmpty(String value) {
        if (value != null && !"".equalsIgnoreCase(value.trim())
                && !"null".equalsIgnoreCase(value.trim())) {
            // 不为空
            return false;
        } else {
            // 为空
            return true;
        }
    }

}
