/*
 * Copyright (c) 2017 Kang Jinoh <jinoh.kang.kr@gmail.com>. All rights reserved.
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
 *
 */

package net.iamahuman.identobf.nodes;

import net.iamahuman.identobf.user.ConstraintException;

import java.util.*;

/**
 * Created by luke1337 on 6/12/17.
 */
public class RequestMeta {
    public final Item item;
    public final Object groupIdentifier;
    public final Object fixedColour;
    public final Collection<? extends Reference<? extends Item>> sameGroupReferences;
    public final Collection<? extends Reference<? extends Item>> sameColourReferences;
    public final Collection<? extends Reference<? extends Item>> differentColourReferences;

    public RequestMeta(Item item, Object groupIdentifier, Object fixedColour,
                       Collection<? extends Reference<? extends Item>> sameGroupReferences,
                       Collection<? extends Reference<? extends Item>> sameColourReferences,
                       Collection<? extends Reference<? extends Item>> differentColourReferences) {
        super();
        if (item == null)
            throw new NullPointerException("item cannot be null");
        this.item = item;
        this.groupIdentifier = groupIdentifier;
        this.fixedColour = fixedColour;
        this.sameGroupReferences = Collections.unmodifiableCollection(sameGroupReferences);
        this.sameColourReferences = Collections.unmodifiableCollection(sameColourReferences);
        this.differentColourReferences = Collections.unmodifiableCollection(differentColourReferences);
    }

    public RequestMeta(RequestMeta meta) {
        super();
        this.item = meta.item;
        this.groupIdentifier = meta.groupIdentifier;
        this.fixedColour = meta.fixedColour;
        this.sameGroupReferences = meta.sameGroupReferences;
        this.sameColourReferences = meta.sameColourReferences;
        this.differentColourReferences = meta.differentColourReferences;
    }

    public static RequestMeta merge(Item item, RequestMeta... sources) throws ConstraintException {
        return merge(item, Arrays.asList(sources));
    }

    public static RequestMeta merge(Item item, Iterable<RequestMeta> sources) throws ConstraintException {
        Object groupIdentifier = null;
        Object fixedColour = null;
        final Collection<Reference<? extends Item>> sameGroupReferences = new HashSet<>();
        final Collection<Reference<? extends Item>> sameColourReferences = new HashSet<>();
        final Collection<Reference<? extends Item>> differentColourReferences = new HashSet<>();

        for (RequestMeta meta : sources) {
            if (meta == null) break;
            if (meta.item != item) throw new ConstraintException("different items");
            if (meta.groupIdentifier != null) {
                if (groupIdentifier == null)
                    groupIdentifier = meta.groupIdentifier;
                else if (!groupIdentifier.equals(meta.groupIdentifier))
                    throw new ConstraintException("distinct group identifier");
            }
            if (meta.fixedColour != null) {
                if (fixedColour == null)
                    fixedColour = meta.fixedColour;
                else if (!fixedColour.equals(meta.fixedColour))
                    throw new ConstraintException("distinct colour fixture");
            }
            sameGroupReferences.addAll(meta.sameGroupReferences);
            sameColourReferences.addAll(meta.sameColourReferences);
            differentColourReferences.addAll(meta.differentColourReferences);
        }
        return new RequestMeta(item, groupIdentifier, fixedColour,
                sameGroupReferences, sameColourReferences, differentColourReferences);
    }

    /**
     * Created by luke1337 on 6/18/17.
     */
    public static class Builder {
        protected Object groupIdentifier = null;
        protected Object fixedColour = null;
        protected Collection<Reference<? extends Item>> sameGroupReferences;
        protected Collection<Reference<? extends Item>> sameColourReferences;
        protected Collection<Reference<? extends Item>> differentColourReferences;

        public Builder() {
            super();
            this.sameGroupReferences = new ArrayList<>();
            this.sameColourReferences = new ArrayList<>();
            this.differentColourReferences = new ArrayList<>();
        }

        public Builder(RequestMeta meta) {
            super();
            this.groupIdentifier = meta.groupIdentifier;
            this.fixedColour = meta.fixedColour;
            this.sameGroupReferences = new ArrayList<>(meta.sameGroupReferences);
            this.sameColourReferences = new ArrayList<>(meta.sameColourReferences);
            this.differentColourReferences = new ArrayList<>(meta.differentColourReferences);
        }

        public Builder apply(RequestMeta meta) throws ConstraintException {
            applyGroupIdentifier(meta.groupIdentifier);
            applyFixedColour(meta.fixedColour);
            addSameGroupReferences(meta.sameGroupReferences);
            addSameColourReferences(meta.sameColourReferences);
            addDifferentColourReferences(meta.differentColourReferences);
            return this;
        }

        public Builder applyGroupIdentifier(Object groupId) throws ConstraintException {
            if (groupId != null) {
                if (this.groupIdentifier == null)
                    this.groupIdentifier = groupId;
                else if (!this.groupIdentifier.equals(groupId))
                    throw new ConstraintException("distinct group id");
            }
            return this;
        }

        public Builder applyFixedColour(Object colour) throws ConstraintException {
            if (colour != null) {
                if (this.fixedColour == null)
                    this.fixedColour = colour;
                else if (!this.fixedColour.equals(colour))
                    throw new ConstraintException("distinct fixed colour (old: " + this.fixedColour + ", new: " + colour + ")");
            }
            return this;
        }

        public Builder addSameGroupReference(Reference<? extends Item> ref) throws ConstraintException {
            sameGroupReferences.add(ref);
            return this;
        }

        public Builder addSameColourReference(Reference<? extends Item> ref) throws ConstraintException {
            sameColourReferences.add(ref);
            return this;
        }

        public Builder addDifferentColourReference(Reference<? extends Item> ref) throws ConstraintException {
            differentColourReferences.add(ref);
            return this;
        }

        public Builder addSameGroupReferences(Collection<? extends Reference<? extends Item>> refs) throws ConstraintException {
            sameGroupReferences.addAll(refs);
            return this;
        }

        public Builder addSameColourReferences(Collection<? extends Reference<? extends Item>> refs) throws ConstraintException {
            sameColourReferences.addAll(refs);
            return this;
        }

        public Builder addDifferentColourReferences(Collection<? extends Reference<? extends Item>> refs) throws ConstraintException {
            differentColourReferences.addAll(refs);
            return this;
        }

        public RequestMeta build(Item item) {
            return new RequestMeta(item, groupIdentifier, fixedColour, sameGroupReferences, sameColourReferences, differentColourReferences);
        }

    }
}
