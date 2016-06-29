package com.example.dawid.dzienniczekauta;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ListaWymian extends AppCompatActivity {

    private ZarzadzanieBazaDanych zarzDB;


    private String nazwaTabeli;
    private String nazwaKolumny;

    private Context context;

    private int editTextDialogType;
    private int layoutDialogu;
    private int layoutDialoguHistoria;

    private Cursor k;
    private String id_auta;

    private LinearLayout rozrzadLL;
    private LinearLayout olejFiltrLL;
    private LinearLayout filtrPowietrzaLL;
    private LinearLayout filtrPaliwaLL;
    private LinearLayout filtrKabinowyLL;
    private LinearLayout ocLL;
    private LinearLayout przegladLL;
    private LinearLayout naprawaLL;

    private TextView rozrzadTV;
    private TextView olejFiltrTV;
    private TextView filtrPowietrzaTV;
    private TextView filtrPaliwaTV;
    private TextView filtrKabinowyTV;
    private TextView ocTV;
    private TextView przegladTV;
    private TextView naprawaTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        id_auta = String.valueOf(intent.getLongExtra("ID_AUTA", -1));

        zarzDB = new ZarzadzanieBazaDanych(this);


        rozrzadLL = (LinearLayout) findViewById(R.id.rozrzadLayout);
        olejFiltrLL = (LinearLayout) findViewById(R.id.olejFiltrLayout);
        filtrPowietrzaLL = (LinearLayout) findViewById(R.id.filtrPowietrzaLayout);
        filtrPaliwaLL = (LinearLayout) findViewById(R.id.filtrPaliwaLayout);
        filtrKabinowyLL = (LinearLayout) findViewById(R.id.filtrKabinowyLayout);
        ocLL = (LinearLayout) findViewById(R.id.ocLayout);
        przegladLL = (LinearLayout) findViewById(R.id.przegladLayout);
        naprawaLL = (LinearLayout) findViewById(R.id.naprawyLayout);

        rozrzadTV = (TextView) findViewById(R.id.rozrzadPrzebiegTextView);
        olejFiltrTV = (TextView) findViewById(R.id.oleFiltrPrzebiegTextView);
        filtrPowietrzaTV = (TextView) findViewById(R.id.filtrPowietrzaTextView);
        filtrPaliwaTV = (TextView) findViewById(R.id.filtrPaliwaTextView);
        filtrKabinowyTV = (TextView) findViewById(R.id.filtrKabinowyTextView);
        ocTV = (TextView) findViewById(R.id.ocTextView);
        przegladTV = (TextView) findViewById(R.id.przegladTextView);
        naprawaTV = (TextView) findViewById(R.id.naprawaTextView);

        rozrzadLL.setOnLongClickListener(historiaWpisow);
        olejFiltrLL.setOnLongClickListener(historiaWpisow);
        filtrPowietrzaLL.setOnLongClickListener(historiaWpisow);
        filtrPaliwaLL.setOnLongClickListener(historiaWpisow);
        filtrKabinowyLL.setOnLongClickListener(historiaWpisow);
        ocLL.setOnLongClickListener(historiaWpisow);
        przegladLL.setOnLongClickListener(historiaWpisow);

        naprawaLL.setOnLongClickListener(historiaNapraw);

        pobierzIWyswietlWszystkieWpisyZBD();

    }

    View.OnLongClickListener historiaNapraw = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.longclick_dialog_entry_repair_history);
            dialog.setTitle("Historia napraw");
            dialog.show();

            final SimpleCursorAdapter adapter = new SimpleCursorAdapter(context,
                    R.layout.listview_item_repair,
                    zarzDB.pobierzNaprawe(id_auta),
                    new String[]{"_id", "opis", "data", "przebieg"},
                    new int[]{R.id.id_wpisu, R.id.opis, R.id.data, R.id.przebieg});

            final ListView listView = (ListView) dialog.findViewById(R.id.listViewDialog);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final Dialog dialog = new Dialog(context);
                    final long id2 = id;
                    dialog.setContentView(R.layout.edit_repair_dialog);
                    dialog.setTitle("Podglad naprawy");

                    Button przyciskUsun = (Button) dialog.findViewById(R.id.button2);
                    przyciskUsun.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            zarzDB.usuwanieWybranegoWpisu("naprawy", String.valueOf(id2), id_auta);
                            dialog.dismiss();
                            adapter.changeCursor(zarzDB.pobierzNaprawe(id_auta));
                            pobierzIWyswietlWszystkieWpisyZBD();
                        }
                    });

                    Button przyciskAktualizuj = (Button) dialog.findViewById(R.id.button);
                    przyciskAktualizuj.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText opis = (EditText) dialog.findViewById(R.id.editTextOpisNaprawy);
                            EditText przebieg = (EditText) dialog.findViewById(R.id.editTextPrzebiegNaprawy);
                            DatePicker data = (DatePicker) dialog.findViewById(R.id.pickerRepairEditDate);
                            String dataString = null;

                            if (opis.getText().toString().isEmpty()) {
                                opis.setError("Nie wpisano opisu naprawy");
                            } else if (przebieg.getText().toString().isEmpty()) {
                                przebieg.setError("Nie wpisanoe przebiegu");
                            } else {
                                String day = String.valueOf(data.getDayOfMonth());
                                if (day.length()==1) {
                                    day ="0"+day;
                                }
                                String month = String.valueOf(data.getMonth()+1);
                                if (month.length()==1) {
                                    month ="0"+month;
                                }
                                dataString = day + "." + month + "." + String.valueOf(data.getYear());
                                zarzDB.aktualizacjaNaprawy(String.valueOf(id2), opis.getText().toString(), dataString, przebieg.getText().toString(), id_auta);
                                dialog.dismiss();
                            }


                            adapter.changeCursor(zarzDB.pobierzNaprawe(id_auta));
                            pobierzIWyswietlWszystkieWpisyZBD();
                        }
                    });

                    dialog.show();
                    EditText opis = (EditText) dialog.findViewById(R.id.editTextOpisNaprawy);
                    EditText przebieg = (EditText) dialog.findViewById(R.id.editTextPrzebiegNaprawy);
                    opis.setText(zarzDB.pobieranieWybranegoWpisu("naprawy", "opis", String.valueOf(id), id_auta));
                    przebieg.setText(zarzDB.pobieranieWybranegoWpisu("naprawy", "przebieg", String.valueOf(id), id_auta));
                    DatePicker data = (DatePicker) dialog.findViewById(R.id.pickerRepairEditDate);

                    String dataWymianyPobranaZBazy = zarzDB.pobieranieDatyWybranegoWpisu("naprawy", String.valueOf(id), id_auta);
                    String[] parts = dataWymianyPobranaZBazy.split("[.]");
                    data.updateDate(Integer.valueOf(parts[2]), Integer.valueOf(parts[1]) - 1, Integer.valueOf(parts[0]));

                }
            });
            return true;
        }
    };

    //przytrzymanie na pozycji, wyswietla historie wpisow - dla wszystkich oprocz napraw
    View.OnLongClickListener historiaWpisow = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final int idLayoutu = v.getId();
            switch (idLayoutu) {
                case R.id.rozrzadLayout:
                    nazwaTabeli = "rozrzad";
                    nazwaKolumny = "przebieg";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_normal_history;
                    break;
                case R.id.olejFiltrLayout:
                    nazwaTabeli = "olej_filtr";
                    nazwaKolumny = "przebieg";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_normal_history;
                    break;
                case R.id.filtrPowietrzaLayout:
                    nazwaTabeli = "filtr_powietrza";
                    nazwaKolumny = "przebieg";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_normal_history;
                    break;
                case R.id.filtrPaliwaLayout:
                    nazwaTabeli = "filtr_paliwa";
                    nazwaKolumny = "przebieg";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_normal_history;
                    break;
                case R.id.filtrKabinowyLayout:
                    nazwaTabeli = "filtr_kabinowy";
                    nazwaKolumny = "przebieg";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_normal_history;
                    break;
                case R.id.ocLayout:
                    nazwaTabeli = "oc";
                    nazwaKolumny = "data_konca";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_date_history;
                    break;
                case R.id.przegladLayout:
                    nazwaTabeli = "przeglad";
                    nazwaKolumny = "data_konca";
                    layoutDialoguHistoria = R.layout.longclick_dialog_entry_date_history;
                    break;
            }

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(layoutDialoguHistoria);
            dialog.setTitle("Historia wpisu: " + nazwaTabeli);
            dialog.show();

            final SimpleCursorAdapter adapter;



            //sprawdzam czy normalny wpis - czy nie jest to oc, przeglads
            if (idLayoutu != R.id.przegladLayout && idLayoutu != R.id.ocLayout) {
                adapter = new SimpleCursorAdapter(context,
                        R.layout.listview_item_normal,
                        zarzDB.pobierzWpisZdata(nazwaKolumny, nazwaTabeli, id_auta),
                        new String[]{"_id", nazwaKolumny, "data"},
                        new int[]{R.id.id_wpisu, R.id.wpis, R.id.data});
            } else {

                adapter = new SimpleCursorAdapter(context,
                        R.layout.listview_item_date,
                        zarzDB.pobierzWpis(nazwaKolumny, nazwaTabeli, id_auta),
                        new String[]{"_id", nazwaKolumny},
                        new int[]{R.id.id_wpisu, R.id.wpis});
            }

            final ListView listView = (ListView) dialog.findViewById(R.id.listViewDialog);

            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final Dialog dialog = new Dialog(context);
                    final long id2 = id;
                    dialog.setContentView(R.layout.edit_normal_dialog);
                    dialog.setTitle("Podglad wpisu: " + nazwaTabeli);

                    Button przyciskUsun = (Button) dialog.findViewById(R.id.button2);
                    przyciskUsun.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            zarzDB.usuwanieWybranegoWpisu(nazwaTabeli, String.valueOf(id2), id_auta);
                            dialog.dismiss();
                            //sprawdzam jaki jest layput listy z historia zeby wiedziec czy poibierac date czy nie
                            if (idLayoutu != R.id.przegladLayout && idLayoutu != R.id.ocLayout) {
                                adapter.changeCursor(zarzDB.pobierzWpisZdata(nazwaKolumny, nazwaTabeli, id_auta));
                            } else {
                                adapter.changeCursor(zarzDB.pobierzWpis(nazwaKolumny, nazwaTabeli, id_auta));
                            }

                            pobierzIWyswietlWszystkieWpisyZBD();
                        }
                    });

                    Button przyciskAktualizuj = (Button) dialog.findViewById(R.id.button);
                    przyciskAktualizuj.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText wpis = (EditText) dialog.findViewById(R.id.editTextWpis);
                            DatePicker data = (DatePicker) dialog.findViewById(R.id.pickerRepairEditDate);
                            String dataString = null;

                            if (idLayoutu != R.id.ocLayout && idLayoutu != R.id.przegladLayout) {
                                if (wpis.getText().toString().isEmpty()) {
                                    wpis.setError("Nie wpisano przebiegu");
                                } else {
                                    String day = String.valueOf(data.getDayOfMonth());
                                    if (day.length()==1) {
                                        day ="0"+day;
                                    }
                                    String month = String.valueOf(data.getMonth()+1);
                                    if (month.length()==1) {
                                        month ="0"+month;
                                    }
                                    dataString = day + "." + month + "." + String.valueOf(data.getYear());
                                    zarzDB.aktualizacjaWybranegoWpisu(nazwaTabeli, String.valueOf(id2), wpis.getText().toString(), dataString, nazwaKolumny, id_auta);
                                    dialog.dismiss();
                                    adapter.changeCursor(zarzDB.pobierzWpisZdata(nazwaKolumny, nazwaTabeli, id_auta));
                                }
                            } else {
                                String day = String.valueOf(data.getDayOfMonth());
                                if (day.length()==1) {
                                    day ="0"+day;
                                }
                                String month = String.valueOf(data.getMonth()+1);
                                if (month.length()==1) {
                                    month ="0"+month;
                                }
                                dataString = day + "." + month + "." + String.valueOf(data.getYear());
                                zarzDB.aktualizacjaWybranegoWpisuBezPrzebiegu(nazwaTabeli, String.valueOf(id2), dataString, id_auta);
                                dialog.dismiss();
                                adapter.changeCursor(zarzDB.pobierzWpis(nazwaKolumny, nazwaTabeli, id_auta));
                            }

                            pobierzIWyswietlWszystkieWpisyZBD();
                        }
                    });

                    dialog.show();
                    EditText wpis = (EditText) dialog.findViewById(R.id.editTextWpis);
                    wpis.setText(zarzDB.pobieranieWybranegoWpisu(nazwaTabeli, nazwaKolumny, String.valueOf(id), id_auta));
                    DatePicker data = (DatePicker) dialog.findViewById(R.id.pickerRepairEditDate);

                    if (idLayoutu != R.id.ocLayout && idLayoutu != R.id.przegladLayout) {
                        String dataWymianyPobranaZBazy = zarzDB.pobieranieDatyWybranegoWpisu(nazwaTabeli, String.valueOf(id), id_auta);
                        String[] parts = dataWymianyPobranaZBazy.split("[.]");
                        data.updateDate(Integer.valueOf(parts[2]), Integer.valueOf(parts[1]) - 1, Integer.valueOf(parts[0]));

                    } else {
                        wpis.setVisibility(View.GONE);
                        TextView tv9 = (TextView) dialog.findViewById(R.id.textView9);
                        tv9.setVisibility(View.GONE);
                        String dataPobranaZBazy = zarzDB.pobieranieWybranegoWpisu(nazwaTabeli, nazwaKolumny, String.valueOf(id), id_auta);
                        String[] parts = dataPobranaZBazy.split("[.]");
                        data.updateDate(Integer.valueOf(parts[2]), Integer.valueOf(parts[1]) - 1, Integer.valueOf(parts[0]));

                    }
                }
            });
            return true;
        }
    };

    //funkcja przy kliknieciu na pozycje - do dodawania wpisu
    public void dodajWpis(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.rozrzadLayout:
                nazwaTabeli = "rozrzad";
                nazwaKolumny = "przebieg";
                editTextDialogType = InputType.TYPE_CLASS_NUMBER;
                layoutDialogu = R.layout.add_entry_dialog_normal;
                break;
            case R.id.olejFiltrLayout:
                nazwaTabeli = "olej_filtr";
                nazwaKolumny = "przebieg";
                editTextDialogType = InputType.TYPE_CLASS_NUMBER;
                layoutDialogu = R.layout.add_entry_dialog_normal;
                break;
            case R.id.filtrPowietrzaLayout:
                nazwaTabeli = "filtr_powietrza";
                nazwaKolumny = "przebieg";
                editTextDialogType = InputType.TYPE_CLASS_NUMBER;
                layoutDialogu = R.layout.add_entry_dialog_normal;
                break;
            case R.id.filtrPaliwaLayout:
                nazwaTabeli = "filtr_paliwa";
                nazwaKolumny = "przebieg";
                editTextDialogType = InputType.TYPE_CLASS_NUMBER;
                layoutDialogu = R.layout.add_entry_dialog_normal;
                break;
            case R.id.filtrKabinowyLayout:
                nazwaTabeli = "filtr_kabinowy";
                nazwaKolumny = "przebieg";
                editTextDialogType = InputType.TYPE_CLASS_NUMBER;
                layoutDialogu = R.layout.add_entry_dialog_normal;
                break;
            case R.id.ocLayout:
                nazwaTabeli = "oc";
                nazwaKolumny = "data_konca";
                editTextDialogType = InputType.TYPE_DATETIME_VARIATION_DATE;
                layoutDialogu = R.layout.add_entry_dialog_date;
                break;
            case R.id.przegladLayout:
                nazwaTabeli = "przeglad";
                nazwaKolumny = "data_konca";
                editTextDialogType = InputType.TYPE_DATETIME_VARIATION_DATE;
                layoutDialogu = R.layout.add_entry_dialog_date;
                break;
            case R.id.naprawyLayout:
                nazwaTabeli = "naprawy";
                nazwaKolumny = "przebieg";
                editTextDialogType = InputType.TYPE_CLASS_NUMBER;
                layoutDialogu = R.layout.add_entry_dialog_repair;
        }

        final Dialog dialog = new Dialog(this);

        dialog.setContentView(layoutDialogu);
        dialog.setTitle("Dodawanie wpisu: " + nazwaTabeli);

        dialog.show();

        if (layoutDialogu == R.layout.add_entry_dialog_normal) {
            final EditText wpisET = (EditText) dialog.findViewById(R.id.dialogEditText);
            wpisET.setInputType(editTextDialogType);
             final DatePicker data = (DatePicker) dialog.findViewById(R.id.pickerDate);


            Button zapisz = (Button) dialog.findViewById(R.id.dialogZapiszButton);
            zapisz.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String wpis = String.valueOf(wpisET.getText());
                    String day = String.valueOf(data.getDayOfMonth());
                    if (day.length()==1) {
                        day ="0"+day;
                    }
                    String month = String.valueOf(data.getMonth()+1);
                    if (month.length()==1) {
                        month ="0"+month;
                    }
                    String dataString = day + "." + month + "." + String.valueOf(data.getYear());
                    if (wpis.isEmpty()) {
                        wpisET.setError("Nie wpisano przebiegu");
                    } else {
                        zarzDB.dodajWpisZDataWymiany(wpis, nazwaKolumny, nazwaTabeli, dataString, id_auta);
                        dialog.dismiss();
                        pobierzIWyswietlWszystkieWpisyZBD();
                    }
                }
            });
        } else if (layoutDialogu == R.layout.add_entry_dialog_repair) {
            final EditText opisET = (EditText) dialog.findViewById(R.id.dialogRepairDescriptionEditText);
            final EditText przebiegET = (EditText) dialog.findViewById(R.id.dialogRepairEditText);
            przebiegET.setInputType(editTextDialogType);

            final DatePicker dataRepair = (DatePicker) dialog.findViewById(R.id.pickerRepairDate);


            Button zapisz = (Button) dialog.findViewById(R.id.dialogRepairZapiszButton);
            zapisz.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String opis = String.valueOf(opisET.getText());
                    String przebieg = String.valueOf(przebiegET.getText());
                    String day = String.valueOf(dataRepair.getDayOfMonth());
                    if (day.length()==1) {
                        day ="0"+day;
                    }
                    String month = String.valueOf(dataRepair.getMonth()+1);
                    if (month.length()==1) {
                        month ="0"+month;
                    }
                    String dataStringRepair = day + "." + month + "." + String.valueOf(dataRepair.getYear());
                    if (opis.isEmpty()) {
                        opisET.setError("Nie wpisano opisu naprawy");
                    } else if (przebieg.isEmpty()) {
                        przebiegET.setError("Nie wpisano przebiegu");
                    } else {
                        zarzDB.dodajWpisZNaprawy(opis,przebieg,dataStringRepair,id_auta);
                        dialog.dismiss();
                        pobierzIWyswietlWszystkieWpisyZBD();
                    }
                }
            });

        } else {
            Button zapisz = (Button) dialog.findViewById(R.id.dialogZapiszButton);
            zapisz.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    DatePicker wyborDaty = (DatePicker) dialog.findViewById(R.id.wyborDaty);
                    String day = String.valueOf(wyborDaty.getDayOfMonth());
                    if (day.length()==1) {
                        day ="0"+day;
                    }
                    String month = String.valueOf(wyborDaty.getMonth()+1);
                    if (month.length()==1) {
                        month ="0"+month;
                    }
                    String wpis = day + "." + month + "." + String.valueOf(wyborDaty.getYear());

                    zarzDB.dodajWpis(wpis, nazwaKolumny, nazwaTabeli, id_auta);
                    dialog.dismiss();
                    pobierzIWyswietlWszystkieWpisyZBD();
                }
            });
        }


    }


    //pobieranie danych na potrrzeby glownego widoku
    public void pobierzIWyswietlWszystkieWpisyZBD() {
        try {
            k = zarzDB.pobierzWpisZdata("przebieg", "rozrzad", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu

            String rozrzad = k.getString(1);
            rozrzadTV.setText(rozrzad + " km");


        } catch (CursorIndexOutOfBoundsException e) {
            rozrzadTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzWpisZdata("przebieg", "olej_filtr", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu
            String olejFiltr = k.getString(1);
            olejFiltrTV.setText(olejFiltr + " km");
        } catch (CursorIndexOutOfBoundsException e) {
            olejFiltrTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzWpisZdata("przebieg", "filtr_powietrza", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu
            String filtrPowietrza = k.getString(1);
            filtrPowietrzaTV.setText(filtrPowietrza + " km");
        } catch (CursorIndexOutOfBoundsException e) {
            filtrPowietrzaTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzWpisZdata("przebieg", "filtr_paliwa", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu
            String filtrPaliwa = k.getString(1);
            filtrPaliwaTV.setText(filtrPaliwa + " km");
        } catch (CursorIndexOutOfBoundsException e) {
            filtrPaliwaTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzWpisZdata("przebieg", "filtr_kabinowy", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu
            String filtrKabinowy = k.getString(1);
            filtrKabinowyTV.setText(filtrKabinowy + " km");
        } catch (CursorIndexOutOfBoundsException e) {
            filtrKabinowyTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzWpis("data_konca", "oc", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu
            String oc = k.getString(1);
            ocTV.setText(oc);
        } catch (CursorIndexOutOfBoundsException e) {
            ocTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzWpis("data_konca", "przeglad", id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu

            String przeglad = k.getString(1);
            przegladTV.setText(przeglad);


        } catch (CursorIndexOutOfBoundsException e) {
            przegladTV.setText(getResources().getString(R.string.label_no_entry));
        }

        try {
            k = zarzDB.pobierzNaprawe(id_auta);
            k.moveToFirst();    //pobiera ostatni wpis
            //int nr = k.getInt(0); //id wpisu

            String przebiegNaprawy = k.getString(1);
            String dataNaprawy = k.getString(2);
            String opisNaprawy = k.getString(3);
            naprawaTV.setText(przebiegNaprawy  + " km - " + opisNaprawy);


        } catch (CursorIndexOutOfBoundsException e) {
            naprawaTV.setText(getResources().getString(R.string.label_no_entry));
        }
    }


}
