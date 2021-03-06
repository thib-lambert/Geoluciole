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

/**
 * Classe TableColumn
 */
class TableColumn {
    private String columnName;
    private String columnType;
    private boolean canBeNull;

    /**
     * Constructeur de la classe
     *
     * @param columnName String nom de la colonne
     * @param columnType String type de la colonne
     * @param canBeNull  boolean si null est a 1 sinon 0
     */
    public TableColumn(String columnName, String columnType, boolean canBeNull) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.canBeNull = canBeNull;
    }

    /**
     * Getter du nom de la colonne
     *
     * @return String
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Setter du nom de la colonne
     *
     * @param columnName String
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Getter du type de la colonne
     *
     * @return String
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * Setter du type de la colonne
     *
     * @param columnType String
     */
    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    /**
     * Getter de la propriété null
     *
     * @return boolean
     */
    public boolean isCanBeNull() {
        return canBeNull;
    }

    /**
     * Setter de la propriété null
     *
     * @param canBeNull boolean
     */
    public void setCanBeNull(boolean canBeNull) {
        this.canBeNull = canBeNull;
    }
}
