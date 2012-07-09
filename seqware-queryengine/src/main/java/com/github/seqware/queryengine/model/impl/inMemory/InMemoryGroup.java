package com.github.seqware.queryengine.model.impl.inMemory;

import com.github.seqware.queryengine.model.Atom;
import com.github.seqware.queryengine.model.Group;
import com.github.seqware.queryengine.model.User;
import com.github.seqware.queryengine.model.impl.AtomImpl;

/**
 * An in-memory representation of a group.
 *
 * @author jbaran
 * @author dyuen
 */
public class InMemoryGroup extends AbstractInMemorySet<Group, User> implements Group{
    
    private String name = null;
    private String description = null;
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override 
    public String getDescription(){
        return description;
    }
    
    /**
     * Override the equals method for Group, kind of a cheat
     * With other objects we take into account things like version or SGID
     * However, with Group, the object is embedded inside the ACL for other objects
     * explicitly, so the version changes too often for easy API use
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
         if (obj instanceof Group) {
            Group other = (Group) obj;
            return this.getName().equals(other.getName()) && this.getDescription().equals(other.getDescription());
        }
        return false;
    }

    
    public static Group.Builder newBuilder() {
        return new InMemoryGroup.Builder();
    }

    @Override
    public InMemoryGroup.Builder toBuilder() {
        InMemoryGroup.Builder b = new InMemoryGroup.Builder();
        b.aSet = (InMemoryGroup) this.copy(true);
        return b;
    }

    @Override
    public Class getHBaseClass() {
        return Group.class;
    }

    @Override
    public String getHBasePrefix() {
        return Group.prefix;
    }

    public static class Builder extends Group.Builder {
        
        public Builder(){
            aSet = new InMemoryGroup();
        }

        @Override
        public Group build(boolean newObject) {
            if(((AtomImpl)aSet).getManager() != null){
            ((AtomImpl)aSet).getManager().objectCreated((Atom)aSet);
            }
            return aSet;
        }

        @Override
        public InMemoryGroup.Builder setName(String name) {
            ((InMemoryGroup)aSet).name = name;
            return this;
        }
        
        @Override
        public InMemoryGroup.Builder setDescription(String description) {
            ((InMemoryGroup)aSet).description = description;
            return this;
        }
    }

}