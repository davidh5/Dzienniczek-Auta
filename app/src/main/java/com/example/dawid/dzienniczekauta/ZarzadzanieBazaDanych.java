package com.example.dawid.dzienniczekauta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Dawid on 18.11.2015.
 */
public class ZarzadzanieBazaDanych extends SQLiteOpenHelper {

    public ZarzadzanieBazaDanych(Context context) {
        super(context, "daneAut.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE auta (_id INTEGER PRIMARY KEY autoincrement, marka text NOT NULL, model text NOT NULL, nr_rejestracyjny text NOT NULL);");

        db.execSQL("CREATE TABLE rozrzad (_id INTEGER PRIMARY KEY autoincrement, przebieg integer NOT NULL, data text NOT NULL, id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE olej_filtr (_id INTEGER PRIMARY KEY autoincrement, przebieg integer NOT NULL, data text NOT NULL, id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE filtr_powietrza (_id INTEGER PRIMARY KEY autoincrement, przebieg integer NOT NULL, data text NOT NULL,id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE filtr_paliwa (_id INTEGER PRIMARY KEY autoincrement, przebieg integer NOT NULL, data text NOT NULL,id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE filtr_kabinowy (_id INTEGER PRIMARY KEY autoincrement, przebieg integer NOT NULL, data text NOT NULL,id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE naprawy (_id INTEGER PRIMARY KEY autoincrement, przebieg integer NOT NULL, opis text NOT NULL, data text NOT NULL, id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE oc (_id INTEGER PRIMARY KEY autoincrement, data_konca text NOT NULL,id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");

        db.execSQL("CREATE TABLE przeglad (_id INTEGER PRIMARY KEY autoincrement, data_konca text NOT NULL,id_auta INTEGER, FOREIGN KEY (id_auta) REFERENCES auta(_id) ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    //wpis - to co ma byc wpisane
    //typ - nazwa kolumny (przebieg lub data_konca)
    //tabela - tabela do ktorej wpisujemy
    public void dodajWpis(String wpis, String typ, String tabela, String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put(typ, wpis);
        dodawaneWartosci.put("id_auta", id);
        db.insertOrThrow(tabela, null, dodawaneWartosci);
    }

    public void dodajWpisZDataWymiany(String wpis, String typ, String tabela, String data, String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put(typ, wpis);
        dodawaneWartosci.put("id_auta", id);
        dodawaneWartosci.put("data", data);
        db.insertOrThrow(tabela, null, dodawaneWartosci);
    }

    public void dodajWpisZNaprawy(String opis, String przebieg , String data, String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put("opis", opis);
        dodawaneWartosci.put("przebieg", przebieg);
        dodawaneWartosci.put("id_auta", id);
        dodawaneWartosci.put("data", data);
        db.insertOrThrow("naprawy", null, dodawaneWartosci);
    }

    public void dodajAuto(String marka, String model, String nr_rejestracyjny) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put("marka", marka);
        dodawaneWartosci.put("model", model);
        dodawaneWartosci.put("nr_rejestracyjny", nr_rejestracyjny);
        db.insertOrThrow("auta", null, dodawaneWartosci);
    }

    // co - przebieg lub data_konca
    //tabela - tabela z ktorej pobieramy
    public Cursor pobierzWpis(String co, String tabela, String id) {
        String[] kolumny = {"_id", co};
        SQLiteDatabase db = getReadableDatabase();
        Cursor kursor = db.query(tabela, kolumny, "id_auta=?", new String[]{id}, null, null, "_id DESC");
        return kursor;
    }

    public Cursor pobierzWpisZdata(String co, String tabela, String id) {
        String[] kolumny = {"_id", co,"data"};
        SQLiteDatabase db = getReadableDatabase();
        Cursor kursor = db.query(tabela, kolumny, "id_auta=?", new String[]{id}, null, null, "przebieg DESC");
        return kursor;
    }


    //pobiera naprawy
    public Cursor pobierzNaprawe(String id) {
        String[] kolumny = {"_id", "przebieg", "data", "opis" };
        SQLiteDatabase db = getReadableDatabase();
        Cursor kursor = db.query("naprawy", kolumny, "id_auta=?", new String[]{id}, null, null, "przebieg DESC");
        return kursor;
    }


    public Cursor pobierzAuto() {
        String[] kolumny = {"_id", "marka", "model", "nr_rejestracyjny"};
        SQLiteDatabase db = getReadableDatabase();
        Cursor kursor = db.query("auta", kolumny, null, null, null, null, null);
        return kursor;

    }


    public void kasowanieBazy() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from rozrzad;");
    }

    //pobieranie przebiegu lub daty na potrzeby wpisania do edittexta w edycji wpisu
    public String pobieranieWybranegoWpisu(String tabela, String kolumna, String id, String id_auta) {
        SQLiteDatabase db = getReadableDatabase();
        Log.e("TEST", id);
        Cursor cursor = db.query(tabela, new String[]{kolumna}, "_id=? AND id_auta=?", new String[]{id, id_auta}, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(kolumna));
    }

    public String pobieranieWybranegoAuta(String kolumna, String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("auta", new String[]{kolumna}, "_id=?", new String[]{id}, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(kolumna));
    }

    //pobieranie daty wymiany na potrzeby wpisania do datepickera w edycji wpisu
    public String pobieranieDatyWybranegoWpisu(String tabela, String id, String id_auta) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(tabela, new String[]{"data"}, "_id=? AND id_auta=?", new String[]{id, id_auta}, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex("data"));
    }

    public void usuwanieWybranegoWpisu(String tabela, String id, String id_auta) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(tabela, "_id = ? AND id_auta=?", new String[]{id, id_auta});
    }

    public void usuwanieWybranegoAuta(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("auta", "_id = ?", new String[]{id});
    }

    //aktualizacja wpisu na potrzeby edycji wpisu (data wymiany + przebieg)
    public void aktualizacjaWybranegoWpisu(String tabela, String id, String doWpisania, String dataDoWpisania, String typ, String id_auta) {
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put(typ, doWpisania);
        dodawaneWartosci.put("data", dataDoWpisania);
        SQLiteDatabase db = getWritableDatabase();
        db.update(tabela, dodawaneWartosci, "_id=? AND id_auta=?", new String[]{id, id_auta});
    }

    public void aktualizacjaNaprawy(String id, String opisDoWpisania, String dataDoWpisania, String przebiegDoWpisania, String id_auta) {
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put("opis", opisDoWpisania);
        dodawaneWartosci.put("data", dataDoWpisania);
        dodawaneWartosci.put("przebieg", przebiegDoWpisania);
        SQLiteDatabase db = getWritableDatabase();
        db.update("naprawy", dodawaneWartosci, "_id=? AND id_auta=?", new String[]{id, id_auta});
    }

    public void aktualizacjaAuta(String id, String modelDoWpisania, String markaDoWpisania, String nrRejestracyjnyDoWpisania) {
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put("marka", markaDoWpisania);
        dodawaneWartosci.put("model", modelDoWpisania);
        dodawaneWartosci.put("nr_rejestracyjny", nrRejestracyjnyDoWpisania);
        SQLiteDatabase db = getWritableDatabase();
        db.update("auta", dodawaneWartosci, "_id=?", new String[]{id});
    }

    public void aktualizacjaWybranegoWpisuBezPrzebiegu(String tabela, String id, String doWpisania, String id_auta) {
        ContentValues dodawaneWartosci = new ContentValues();
        dodawaneWartosci.put("data_konca", doWpisania);
        SQLiteDatabase db = getWritableDatabase();
        db.update(tabela, dodawaneWartosci, "_id=? AND id_auta=?", new String[]{id, id_auta});
    }


    //pobiera dla wszystkich aut na potrzeby eksportu do txt
    public Cursor pobierzTabele(String tabela) {
        SQLiteDatabase db = getReadableDatabase();
        String[] kolumnyOcPrzeglad = {"_id", "data_konca", "id_auta"};
        String[] kolumnyPozostale = {"_id", "przebieg", "id_auta", "data"};
        String [] kolumnyNaprawy = {"_id", "przebieg", "opis", "data", "id_auta"};
        Cursor kursor = null;
        switch (tabela) {
            case "oc":
                kursor = db.query(tabela, kolumnyOcPrzeglad, null, null, null, null, "_id DESC");
                break;
            case "przeglad":
                kursor = db.query(tabela, kolumnyOcPrzeglad, null, null, null, null, "_id DESC");
                break;
            case "naprawy":
                kursor = db.query(tabela, kolumnyNaprawy, null, null, null, null, "przebieg DESC");
                break;
            default:
                kursor = db.query(tabela, kolumnyPozostale, null, null, null, null, "przebieg DESC");
                break;
        }
//        if (tabela.equals("oc") || tabela.equals("przeglad")) {
//            kursor = db.query(tabela, kolumny, null, null, null, null, null);
//        } else {
//            kursor = db.query(tabela, kolumny2, null, null, null, null, null);
//        }

        return kursor;
    }

    public Cursor pobierzTabeleAuta() {
        String[] kolumny = {"_id", "marka", "model", "nr_rejestracyjny"};
        SQLiteDatabase db = getReadableDatabase();
        Cursor kursor = db.query("auta", kolumny, null, null, null, null, null);
        return kursor;
    }

}
