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

package net.iamahuman.identobf.impl.asm;

import net.iamahuman.identobf.annots.*;
import net.iamahuman.identobf.annots.Group;
import net.iamahuman.identobf.nodes.*;
import net.iamahuman.identobf.user.ConstraintException;
import net.iamahuman.identobf.user.RequestRegistry;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

/**
 * Created by luke1337 on 6/18/17.
 */
public class AsmAnnotationAdapter implements RequestMetaAdapter {

    private static <T> Map<T, T> processPairs(List<? extends T> pairs) {
        Map<T, T> map = new HashMap<>();
        T key = null;
        boolean isKey = true;
        for (T either : pairs) {
            if (either == null)
                throw new NullPointerException();
            if (isKey) {
                key = either;
            } else {
                map.put(key, either);
            }
            isKey = !isKey;
        }
        if (!isKey)
            throw new IllegalArgumentException("Odd-length pairs");
        return map;
    }

    private static Collection<Reference<? extends Item>> processReferenceAnnotation(Item item, AnnotationNode node, Class<?> annotClass) throws ConstraintException {
        if (!annotClass.isAnnotation())
            throw new IllegalArgumentException("annotClass must be an annotation type");
        if (!("L" + annotClass.getName().replace('.', '/') + ";").equals(node.desc))
            throw new IllegalArgumentException("annot class mismatch");
        Map<Object, Object> map = processPairs(node.values);
        Object value = map.get("value");
        if (!(value instanceof List))
            throw new ConstraintException("value not given");
        Object weakBoxed = map.get("weakBoxed");
        if (weakBoxed == null) {
            try {
                weakBoxed = annotClass.getDeclaredMethod("weak").getDefaultValue();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        if (!(weakBoxed instanceof Boolean))
            throw new ConstraintException("Invalid or null weak property type");
        boolean isStrong = !(boolean) weakBoxed;
        Collection<Reference<? extends Item>> refs = new ArrayList<>();
        for (Object ref : (List<?>) value) {
            String id = null;
            if (ref instanceof String)
                id = ((String) ref).replace(".", "/");
            else if (ref instanceof Type) {
                Type typ = (Type) ref;
                if (typ.getSort() == Type.OBJECT) {
                    id = typ.getInternalName();
                    if (id == null) throw new NullPointerException();
                }
            }
            if (id == null)
                throw new ConstraintException("Invalid type specified in value: " + ref + " (" + ref.getClass() + ")");
            refs.add(new ReferenceProxy<>(item, id, isStrong)); // TODO use legitimate one
        }
        return refs;
    }

    public boolean processAnnotation(RequestMeta.Builder builder, Item item, AnnotationNode annot) throws ConstraintException {
        String rawDesc = annot.desc;
        if (!rawDesc.startsWith("L") || !rawDesc.endsWith(";"))
            return false;
        String desc = rawDesc.substring(1, rawDesc.length() - 1).replace('/', '.');
        if (Group.class.getName().equals(desc)) {
            Map<Object, Object> map = processPairs(annot.values);
            Object forcedGroupId = map.get("value");
            if (forcedGroupId == null)
                throw new ConstraintException("value not given for annotation Colour");
            builder.applyGroupIdentifier(forcedGroupId);
        } else if (Colour.class.getName().equals(desc)) {
            Map<Object, Object> map = processPairs(annot.values);
            Object fixedColour = map.get("value");
            if (fixedColour == null)
                throw new ConstraintException("value not given for annotation Colour");
            builder.applyFixedColour(fixedColour);
        } else if (SameGroup.class.getName().equals(desc)) {
            builder.addSameGroupReferences(processReferenceAnnotation(item, annot, SameGroup.class));
        } else if (SameColour.class.getName().equals(desc)) {
            builder.addSameColourReferences(processReferenceAnnotation(item, annot, SameColour.class));
        } else if (DistinctColour.class.getName().equals(desc)) {
            builder.addDifferentColourReferences(processReferenceAnnotation(item, annot, DistinctColour.class));
        } else return false;
        return true;
    }

    private void processAnnotationsAndRemove(RequestMeta.Builder builder, Item item, Iterable<? extends AnnotationNode> annots) throws ConstraintException {
        for (Iterator<? extends AnnotationNode> it = annots.iterator(); it.hasNext(); ) {
            AnnotationNode anode = it.next();
            if (processAnnotation(builder, item, anode))
                it.remove();
        }
    }

    @Override
    public RequestMeta getMeta(RequestRegistry registry, Item item) throws ConstraintException {
        if (!(item instanceof AsmItem))
            throw new UnsupportedOperationException(getClass().getName() + " only supports AsmItem");

        RequestMeta.Builder builder = new RequestMeta.Builder();
        {
            ClassNode node = ((AsmItem) item).getResult();
            if (node.visibleAnnotations != null)
                processAnnotationsAndRemove(builder, item, node.visibleAnnotations);
            if (node.invisibleAnnotations != null)
                processAnnotationsAndRemove(builder, item, node.invisibleAnnotations);
        }
        return builder.build(item);
    }
}
