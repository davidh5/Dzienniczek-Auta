package com.example.dawid.dzienniczekauta;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WyborAuta extends AppCompatActivity {

    private ZarzadzanieBazaDanych zarzDB;
    private ListView listViewAuta;
    private Context context;
    private FileChannel src = null;
    private FileChannel dst = null;
    private String chosenFileToImport = null;

    private String[] tabele = {"rozrzad", "olej_filtr", "filtr_powietrza", "filtr_paliwa", "filtr_kabinowy", "oc", "przeglad", "naprawy"};

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String path = data.getData().getPath();
            if (path.contains("daneAut.db")) {
                chosenFileToImport = path;
                importDB(chosenFileToImport);
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_wrong_file), Toast.LENGTH_LONG).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wybor_auta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;
        zarzDB = new ZarzadzanieBazaDanych(this);
        listViewAuta = (ListView) findViewById(R.id.listViewAuta);
        listViewAuta.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, ListaWymian.class);
                intent.putExtra("ID_AUTA", id);
                startActivity(intent);
            }
        });
        listViewAuta.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.edit_car_dialog);
                dialog.setTitle("Edycja auta");
                dialog.show();
                final long id2 = id;

                Button przyciskUsun = (Button) dialog.findViewById(R.id.button4);
                przyciskUsun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zarzDB.usuwanieWybranegoAuta(String.valueOf(id2));
                        dialog.dismiss();
                        pobierzIWyswietlAuta();
                    }
                });

                Button przyciskAktualizuj = (Button) dialog.findViewById(R.id.button3);
                przyciskAktualizuj.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText marka = (EditText) dialog.findViewById(R.id.markaEdit_editDialog);
                        EditText model = (EditText) dialog.findViewById(R.id.modelEdit_editDialog);
                        EditText nrRejestracyjny = (EditText) dialog.findViewById(R.id.nrRejestracyjnyEdit_editDialog);

                        if (marka.getText().toString().isEmpty()) {
                            marka.setError("Nie wpisano marki samochodu");
                        } else if (model.getText().toString().isEmpty()) {
                            model.setError("Nie wpisano modelu samochodu");
                        }else if (nrRejestracyjny.getText().toString().isEmpty()) {
                            nrRejestracyjny.setError("Nie wpisano numeru rejestracyjnego samochodu");
                        }
                            else {
                            zarzDB.aktualizacjaAuta(String.valueOf(id2), model.getText().toString(), marka.getText().toString(), nrRejestracyjny.getText().toString());
                            dialog.dismiss();
                        }
                        pobierzIWyswietlAuta();
                    }
                });


                EditText marka = (EditText) dialog.findViewById(R.id.markaEdit_editDialog);
                EditText model = (EditText) dialog.findViewById(R.id.modelEdit_editDialog);
                EditText nrRejestracyjny = (EditText) dialog.findViewById(R.id.nrRejestracyjnyEdit_editDialog);
                marka.setText(zarzDB.pobieranieWybranegoAuta("marka", String.valueOf(id2)));
                model.setText(zarzDB.pobieranieWybranegoAuta("model", String.valueOf(id2)));
                nrRejestracyjny.setText(zarzDB.pobieranieWybranegoAuta("nr_rejestracyjny", String.valueOf(id2)));
                return true;
            }
        });
        pobierzIWyswietlAuta();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_export_txt) {
            try {
                eksportujWpisyDoTXT();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.menu_export_db) {
            exportDB();
            return true;
        }
        if (id == R.id.menu_import_db) {
            openFile("*/*");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openFile(String minmeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Wybierz plik bazy danych");
            //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Wybierz plik bazy danych");
        }


        try {
            startActivityForResult(chooserIntent, 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Brak eksploratora plików na urządzeniu", Toast.LENGTH_LONG).show();
        }
    }


    public void dodajAuto(View view) {
        final Dialog dialog_dodaj_auto = new Dialog(this);
        dialog_dodaj_auto.setContentView(R.layout.add_car_dialog);
        dialog_dodaj_auto.setTitle("Dodaj auto");
        dialog_dodaj_auto.show();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        Button dodajAuto = (Button) dialog_dodaj_auto.findViewById(R.id.dialogDodajAutoDodaj);
        dodajAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText markaET = (EditText) dialog_dodaj_auto.findViewById(R.id.markaEdit);
                EditText modelET = (EditText) dialog_dodaj_auto.findViewById(R.id.modelEdit);
                EditText rejET = (EditText) dialog_dodaj_auto.findViewById(R.id.nrRejestracyjnyEdit);

                if (markaET.getText().toString().isEmpty()) {
                    markaET.setError("Nie wpisano marki auta");
                } else if (modelET.getText().toString().isEmpty()) {
                    modelET.setError("Nie wpisano modelu auta");
                } else if (rejET.getText().toString().isEmpty()) {
                    rejET.setError("Nie wpisano numeru rejestracyjnego auta");
                } else {
                    zarzDB.dodajAuto(markaET.getText().toString(), modelET.getText().toString(), rejET.getText().toString());
                    pobierzIWyswietlAuta();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    dialog_dodaj_auto.dismiss();
                }

            }
        });
    }

    public void pobierzIWyswietlAuta() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.listview_item_car,
                zarzDB.pobierzAuto(),
                new String[]{"marka", "model", "nr_rejestracyjny"},
                new int[]{R.id.marka, R.id.model, R.id.nr_rejestracyjny});
        listViewAuta.setAdapter(adapter);
    }


    // obsluzyc trzeba nowe pole w bazie i ogolnie ogarnac
    public void eksportujWpisyDoTXT() throws IOException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        String TimeStampDB = sdf.format(cal.getTime());

        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath());
            directory.mkdirs();

            File file = new File(directory, "Dzienniczek_Auta_Dane_" + TimeStampDB + ".txt");
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            Cursor c = zarzDB.pobierzTabeleAuta();
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        String idWpisu = c.getString(c.getColumnIndex("_id"));
                        String marka = c.getString(c.getColumnIndex("marka"));
                        String model = c.getString(c.getColumnIndex("model"));
                        String nr_rej = c.getString(c.getColumnIndex("nr_rejestracyjny"));


                        osw.write("auto:   " + idWpisu + "; " + marka + "; " + model + "; " + nr_rej + "\r\n");
                        osw.flush();
                    }
                    while (c.moveToNext());
                }
                c.close();
            }

            osw.flush();
            for (String tabela : tabele) {
                c = zarzDB.pobierzTabele(tabela);
                if (c != null) {
                    if (c.moveToFirst()) {
                        do {
                            String idWpisu = c.getString(c.getColumnIndex("_id"));
                            String wpis;
                            String data_wymiany;
                            String idAuta = c.getString(c.getColumnIndex("id_auta"));
                            if (tabela.equals("oc") || tabela.equals("przeglad")) {
                                wpis = c.getString(c.getColumnIndex("data_konca"));
                                osw.write(tabela + ":   " + idWpisu + "; " + wpis + "; " + idAuta + "\r\n");
                            } else if (tabela.equals("naprawy")) {
                                wpis = c.getString(c.getColumnIndex("przebieg"));
                                String opis = c.getString(c.getColumnIndex("opis"));
                                data_wymiany = c.getString(c.getColumnIndex("data"));
                                osw.write(tabela + ":   " + idWpisu + "; " + wpis + "; "+ opis + "; " + data_wymiany + "; " + idAuta + "\r\n");
                            }  else {
                                wpis = c.getString(c.getColumnIndex("przebieg"));
                                data_wymiany = c.getString(c.getColumnIndex("data"));
                                osw.write(tabela + ":   " + idWpisu + "; " + wpis + "; " + data_wymiany + "; " + idAuta + "\r\n");
                            }

                            osw.flush();
                        }
                        while (c.moveToNext());
                    }
                    c.close();
                }
            }
            osw.close();
            fOut.close();
            Toast.makeText(WyborAuta.this, "Eksportowano dane do pliku txt", Toast.LENGTH_LONG).show();
        } catch (SQLiteException se) {
        }
    }

    private void importDB(String file) {
        // TODO Auto-generated method stub

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "com.example.dawid.dzienniczekauta"
                        + "//databases//" + "daneAut.db";
                File backupDB = new File(data, currentDBPath);
                File currentDB = new File(file);

                src = new FileInputStream(currentDB).getChannel();
                dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_import_success),
                        Toast.LENGTH_LONG).show();
                pobierzIWyswietlAuta();

            }
        } catch (Exception e) {
            try {
                src.close();
                dst.close();
            } catch (IOException e1) {
            }
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_import_failure), Toast.LENGTH_LONG)
                    .show();

        }
    }

    //exporting database
    private void exportDB() {
        // TODO Auto-generated method stub
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        String TimeStampDB = sdf.format(cal.getTime());
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();


            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "com.example.dawid.dzienniczekauta"
                        + "//databases//" + "daneAut.db";
                String backupDBPath = "/backup_" + TimeStampDB + "_daneAut.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                src = new FileInputStream(currentDB).getChannel();
                dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_export_success),
                        Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {

            try {
                src.close();
                dst.close();
            } catch (IOException e1) {
            }
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_export_failure), Toast.LENGTH_LONG)
                    .show();

        }
    }
}
