/*
 * Copyright (c) 2020, Martin Allusse and Alexandre Baret and Jessy Barritault and Florian
 * Bertonnier and Lisa Fougeron and François Gréau and Thibaud Lambert and Antoine
 * Orgerit and Laurent Rayez
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *  Neither the name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fr.univ_lr.geoluciole.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe Table abstraite
 */
public abstract class Table {
    String tableName = "";
    // colonnes de la table
    TableColumn[] columns;
    DatabaseHandler dbSQLite;
    Context context;

    Table() {
    }

    /**
     * Constructeur de la Table
     *
     * @param context Context
     */
    Table(Context context) {
        dbSQLite = DatabaseHandler.getInstance(context);
        this.context = context;
    }

    /**
     * Permet de créer une table en base de données dynamiquement
     *
     * @return String la requete de création
     */
    public String prepareSQLForCreateTable() {
        StringBuilder sqlRequest = new StringBuilder("CREATE TABLE IF NOT EXISTS");
        sqlRequest.append(" ").append(this.tableName).append("(");

        int endIndex = this.columns.length;
        for (int i = 0; i < endIndex; i++) {

            TableColumn column = this.columns[i];

            sqlRequest.append(" ").append(column.getColumnName());
            sqlRequest.append(" ").append(column.getColumnType());

            if (!column.isCanBeNull()) {
                sqlRequest.append(" NOT NULL");
            }

            if (i != columns.length - 1) {
                sqlRequest.append(",");
            }
        }
        sqlRequest.append(" );");
        return sqlRequest.toString();
    }

    /**
     * Permet de supprimer une table en base de données
     *
     * @return String la requete de suppression
     */
    String prepareSQLForDeleteTable() {
        return "DROP TABLE IF EXISTS " + this.tableName + ";";
    }

    /**
     * Permet d insérer un objet en base
     *
     * @param o Object a sauvegarder
     */
    public void insert(Object o) {
        this.dbSQLite.open();
        try {
            insertObject(o);
        } catch (Exception e) {
            // do nothing
        }
        this.dbSQLite.close();
    }

    /**
     * Permet d inserer un objet en base
     *
     * @param o Object a sauvegarder
     */
    protected abstract void insertObject(Object o);

    /**
     * Permet de supprimer tous les objets en base
     */
    public void removeAll() {
        this.dbSQLite.open();
        try {
            removeAllObject();
        } catch (Exception ex) {
            // do nothing
        }
        this.dbSQLite.close();
    }

    /**
     * Permet de supprimer tous les objets en base
     */
    protected abstract void removeAllObject();

    /**
     * Permet de recuperer tous les objets de la base
     *
     * @return List des objets
     */
    public List getAll() {
        List list = new ArrayList();
        this.dbSQLite.open();
        try {
            list = getAllObject();
        } catch (Exception ex) {
            //do nothing
        }
        this.dbSQLite.close();
        return list;
    }

    /**
     * Permet de recuperer tous les objets de la base
     *
     * @return List des objets
     */
    protected abstract List getAllObject();
}
