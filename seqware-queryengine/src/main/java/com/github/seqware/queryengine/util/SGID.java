/*
 * Copyright (C) 2012 SeqWare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.seqware.queryengine.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.seqware.queryengine.impl.StorageInterface;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * A wrapper for our eventual choice of globally unique primary key which may or
 * may not be UUIDs.
 *
 * The primary key is paired with a timestamp, which is currently populated by
 * HBase. This allows the back-end to perform two kinds of queries, using only
 * the primary key portion to retrieve all the versions of a particular version
 * chain or using both the primary key and the timestamp for direct access to
 * one specific version. HBase also allows us to quickly retrieve the latest
 * version using only the primary key.
 *
 * @author dyuen
 */
public class SGID implements Serializable, KryoSerializable {

    /**
     * use as a primary key, this is now unique within a version chain
     */
    private UUID uuid = null;
    /**
     * generated by the back-end, this can be paired with uuid to provide a
     * unique ID. This doesn't really need to be stored in the HBase back-end,
     * it is done for us
     */
    private Date backendTimestamp;
    /**
     * this can be used to override the randomly generated IDs for the back-end.
     * However, this can be dangerous allowing the overwriting of known entries.
     * Will need safety checks.
     */
    protected String friendlyRowKey = null;

    /**
     * Create a new SGID
     */
    public SGID() {
        uuid = UUID.randomUUID();
        backendTimestamp = new Date();
    }
    
    /**
     * Generate a SGID that can look up things given only a rowKey.
     * @param rowKey 
     */
    public SGID(String rowKey){
        friendlyRowKey = rowKey;
    }

    /**
     * Clone a given SGID
     *
     * @param sgid
     */
    public SGID(SGID sgid) {
        uuid = new UUID(sgid.uuid.getMostSignificantBits(), sgid.uuid.getLeastSignificantBits());
        backendTimestamp = new Date(sgid.getBackendTimestamp().getTime());
        friendlyRowKey = sgid.friendlyRowKey;
    }

    /**
     * Back-end constructor, create a fully functional SGID
     */
    public SGID(long mostSig, long leastSig, long timestamp, String friendlyRowKey) {
        uuid = new UUID(mostSig, leastSig);
        backendTimestamp = new Date(timestamp);
        this.friendlyRowKey = friendlyRowKey;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SGID) {
            SGID a = (SGID) other;
            boolean uidEq = this.uuid.equals(a.uuid);
            if (!uidEq) {
            return false;
        }
            return this.backendTimestamp.equals(a.backendTimestamp);
        }
            return false;
        }

    @Override
    public int hashCode() {
        return uuid.hashCode() + backendTimestamp.hashCode();
    }

    @Override
    public String toString() {
        //throw new UnsupportedOperationException();
//        if (this.friendlyRowKey != null){
//            return this.friendlyRowKey +  StorageInterface.SEPARATOR + backendTimestamp.getTime();
//        }
        return uuid.toString() + StorageInterface.SEPARATOR + backendTimestamp.getTime();
    }

    /**
     * Get underlying UUID implementation, should only be called within the
     * back-end
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Output the portion of the SGID that is used as a rowKey
     *
     * @return
     */
    public String getRowKey() {
        if (this.friendlyRowKey != null){
            return this.friendlyRowKey;
        }
        return uuid.toString();
    }

    public Date getBackendTimestamp() {
        return backendTimestamp;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(uuid.getLeastSignificantBits());
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(backendTimestamp.getTime());
        output.writeString(friendlyRowKey);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        long leastSig = input.readLong();
        long mostSig = input.readLong();
        String key = input.readString();
        uuid = new UUID(mostSig, leastSig);
        backendTimestamp = new Date(input.readLong());
        friendlyRowKey = key;
    }

    protected void setUuid(UUID uuiD) {
        this.uuid = uuiD;
    }

    public void setBackendTimestamp(Date date) {
        this.backendTimestamp = date;
    }

    public String getFriendlyRowKey() {
        return friendlyRowKey;
    }

    public void setFriendlyRowKey(String friendlyRowKey) {
        this.friendlyRowKey = friendlyRowKey;
    }
    
    
}
