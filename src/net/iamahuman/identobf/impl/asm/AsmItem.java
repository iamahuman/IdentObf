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

import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.Reference;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by luke1337 on 6/14/17.
 */
public class AsmItem extends Item {
    final Config config;

    private ClassNode classNode;

    private final Object originalIdentifier;
    final Queue<AsmDescriptor> dirtyDescs = new ConcurrentLinkedQueue<>();

    public AsmItem(Config config, ClassNode classNode, Object originalIdentifier) {
        super();
        this.config = config;
        this.classNode = classNode;
        this.originalIdentifier = originalIdentifier;
    }

    @Override
    public void forEachReference(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        yieldSelfReference(visitor);
        yieldInheritReference(visitor);
        yieldEnclosingMethodReference(visitor);
        yieldInnerClassReferences(visitor);
        yieldInterfaceReferences(visitor);
        yieldSignatureReferences(visitor);
        yieldAnnotationReferences(visitor, classNode.visibleAnnotations);
        yieldAnnotationReferences(visitor, classNode.visibleTypeAnnotations);
        yieldAnnotationReferences(visitor, classNode.invisibleAnnotations);
        yieldAnnotationReferences(visitor, classNode.invisibleTypeAnnotations);
        yieldOuterClassReferences(visitor);
        for (FieldNode f : classNode.fields)
            yieldFieldReferences(visitor, f);
        for (MethodNode m : classNode.methods)
            yieldMethodReferences(visitor, m);
    }

    protected static void acceptNullable(Reference.Visitor visitor, Reference<? extends Item> ref) throws Reference.Visitor.Stop {
        if (ref.getIdentifier() != null)
            visitor.visit(ref);
    }

    protected void yieldSelfReference(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        visitor.visit(new AsmSelfReference(this, classNode, false));
    }

    protected void yieldInheritReference(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        if (classNode.superName != null)
            visitor.visit(new AsmInheritReference(this, classNode, true));
    }

    protected void yieldEnclosingMethodReference(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        new AsmOuterMethodDescriptor(this, classNode, true).parseAndAccept(visitor);
    }

    protected void yieldInterfaceReferences(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        List<String> interfaces = classNode.interfaces;
        // XXX is this nullable?
        if (interfaces != null) yieldInternalNameReferences(visitor, interfaces);
    }

    protected void yieldInternalNameReferences(Reference.Visitor visitor, List<String> names) throws Reference.Visitor.Stop {
        for (int i = 0, s = names.size(); i < s; i++)
            visitor.visit(new AsmGenericListReference(this, names, i, AsmGenericListReference.REF_CLASSONLY, true));
    }

    protected void yieldAnnotationReferences(Reference.Visitor visitor, Iterable<? extends AnnotationNode> annots) throws Reference.Visitor.Stop {
        if (annots != null) for (AnnotationNode node : annots) {
            visitor.visit(new AsmAnnotationReference(this, node, true));
            if (node.values != null)
                yieldAnnotationValuesReferences(visitor, node.values);
        }
    }

    protected void yieldInnerClassReferences(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        for (InnerClassNode node : classNode.innerClasses) {
            if (node.name != null)
                visitor.visit(new AsmInnerClassReference(this, node, AsmInnerClassReference.RT_INNER_NAME, true));
            if (node.outerName != null)
                visitor.visit(new AsmInnerClassReference(this, node, AsmInnerClassReference.RT_OUTER_NAME, true));
        }
    }

    protected void yieldOuterClassReferences(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        if (classNode.outerClass != null)
            visitor.visit(new AsmOuterClassReference(this, classNode, true));
    }

    protected void yieldSignatureReferences(Reference.Visitor visitor) throws Reference.Visitor.Stop {
        new AsmSignatureDescriptor(this, true, classNode).parseAndAccept(visitor);
    }

    protected void yieldMethodReferences(Reference.Visitor visitor, MethodNode node) throws Reference.Visitor.Stop {
        yieldRawMethodSignatureReferences(visitor, node);
        yieldMethodSignatureReferences(visitor, node);
        if (node.localVariables != null) for (LocalVariableNode lvar : node.localVariables)
            yieldLocalVariableReferences(visitor, lvar);
        yieldMethodAnnotationDefault(visitor, node);
        yieldAnnotationReferences(visitor, node.invisibleAnnotations);
        yieldAnnotationReferences(visitor, node.invisibleLocalVariableAnnotations);
        yieldAnnotationReferences(visitor, node.invisibleTypeAnnotations);
        if (node.invisibleParameterAnnotations != null)
            for (Iterable<? extends AnnotationNode> it : node.invisibleParameterAnnotations)
                yieldAnnotationReferences(visitor, it);
        yieldAnnotationReferences(visitor, node.visibleAnnotations);
        yieldAnnotationReferences(visitor, node.visibleLocalVariableAnnotations);
        yieldAnnotationReferences(visitor, node.visibleTypeAnnotations);
        if (node.visibleParameterAnnotations != null)
            for (Iterable<? extends AnnotationNode> it : node.visibleParameterAnnotations)
                yieldAnnotationReferences(visitor, it);
        yieldInternalNameReferences(visitor, node.exceptions);
        for (TryCatchBlockNode tcb : node.tryCatchBlocks)
            yieldTryCatchBlockReferences(visitor, tcb);
        yieldInsnListReferences(visitor, node.instructions);
    }

    protected void yieldMethodAnnotationDefault(Reference.Visitor visitor, MethodNode node) throws Reference.Visitor.Stop {
        // TODO enums
        Object obj = node.annotationDefault;
        if (obj instanceof Type) {
            Type typ = (Type) obj;
            switch (typ.getSort()) {
                case Type.OBJECT:case Type.ARRAY:
                    acceptNullable(visitor, new AsmAnnotDefTypeReference(this, node, true));
                    break;
                case Type.METHOD:
                    new AsmAnnotDefTypeDescriptor(this, node, true).parseAndAccept(visitor);
                    break;
            }
        } else if (obj instanceof AnnotationNode) {
            visitor.visit(new AsmAnnotationReference(this, (AnnotationNode) obj, true));
        } else if (obj instanceof List) {
            // list must be writable, that's why we explicitly specify it as List<Object>
            //noinspection unchecked
            yieldAnnotationValuesReferences(visitor, (List<Object>) obj);
        }
    }

    protected void yieldAnnotationValuesReferences(Reference.Visitor visitor, List<Object> list) throws Reference.Visitor.Stop {
        for (int i = 0, s = list.size(); i < s; i++) {
            Object item = list.get(i);
            if (item instanceof Type) {
                Type typ = (Type) item;
                switch (typ.getSort()) {
                    case Type.OBJECT:case Type.ARRAY:
                        acceptNullable(visitor, new AsmAnnotDefTypeListReference(this, list, i, true));
                        break;
                    case Type.METHOD:
                        new AsmAnnotDefTypeListDescriptor(this, list, i, true).parseAndAccept(visitor);
                        break;
                }
            } else if (item instanceof AnnotationNode) {
                visitor.visit(new AsmAnnotationReference(this, (AnnotationNode) item, true));
            } else if (item instanceof List) {
                //noinspection unchecked
                yieldAnnotationValuesReferences(visitor, (List<Object>) item);
            }
        }

    }

    protected void yieldLocalVariableReferences(Reference.Visitor visitor, LocalVariableNode lvar) throws Reference.Visitor.Stop {
        acceptNullable(visitor, new AsmLVarDescReference(this, lvar, true));
        new AsmLVarTypeDescriptor(this, true, lvar).parseAndAccept(visitor);
    }

    protected void yieldTryCatchBlockReferences(Reference.Visitor visitor, TryCatchBlockNode node) throws Reference.Visitor.Stop {
        yieldAnnotationReferences(visitor, node.invisibleTypeAnnotations);
        yieldAnnotationReferences(visitor, node.visibleTypeAnnotations);
        acceptNullable(visitor, new AsmTryCatchBlockTypeReference(this, node, true));
    }

    protected void yieldMethodSignatureReferences(Reference.Visitor visitor, MethodNode node) throws Reference.Visitor.Stop {
        new AsmMethodDescriptor(this, true, node).parseAndAccept(visitor);
    }

    protected void yieldRawMethodSignatureReferences(Reference.Visitor visitor, MethodNode node) throws Reference.Visitor.Stop {
        new AsmRawMethodDescriptor(this, true, node).parseAndAccept(visitor);
    }

    protected void yieldInsnListReferences(Reference.Visitor visitor, InsnList list) throws Reference.Visitor.Stop {
        final int size = list.size();
        for (int i = 0; i < size; i++) {
            AbstractInsnNode insn = list.get(i);
            yieldInsnReference(visitor, insn);
        }
    }

    protected void yieldInsnReference(Reference.Visitor visitor, AbstractInsnNode insn) throws Reference.Visitor.Stop {
        yieldAnnotationReferences(visitor, insn.visibleTypeAnnotations);
        yieldAnnotationReferences(visitor, insn.invisibleTypeAnnotations);
        switch (insn.getType()) {
            case AbstractInsnNode.LDC_INSN: {
                LdcInsnNode qins = (LdcInsnNode) insn;
                AsmLdcInsnReference prim = new AsmLdcInsnReference(this, qins, true);
                if (prim.getIdentifier() != null) {
                    visitor.visit(prim);
                } else {
                    new AsmLdcRefDescriptor(this, true, qins).parseAndAccept(visitor);
                }
                break;
            }
            case AbstractInsnNode.FIELD_INSN: {
                FieldInsnNode qins = (FieldInsnNode) insn;
                visitor.visit(new AsmFieldInsnOwnerReference(this, qins, true));
                new AsmFieldInsnDescriptor(this, true, qins).parseAndAccept(visitor);
                break;
            }
            case AbstractInsnNode.METHOD_INSN: {
                MethodInsnNode qins = (MethodInsnNode) insn;
                visitor.visit(new AsmMethodInsnOwnerReference(this, qins, true));
                new AsmMethodInsnDescriptor(this, true, qins).parseAndAccept(visitor);
                break;
            }
            case AbstractInsnNode.TYPE_INSN: {
                TypeInsnNode qins = (TypeInsnNode) insn;
                if (qins.desc != null)
                    acceptNullable(visitor, new AsmTypeInsnReference(this, qins, true));
                break;
            }
            case AbstractInsnNode.MULTIANEWARRAY_INSN: {
                MultiANewArrayInsnNode mainsn = (MultiANewArrayInsnNode) insn;
                visitor.visit(new AsmMultiANewArrayInsnReference(this, mainsn, true));
                break;
            }
            case AbstractInsnNode.FRAME: {
                FrameNode qins = (FrameNode) insn;
                {
                    final List<Object> local = qins.local;
                    if (local != null) for (int i = 0; i < local.size(); i++) {
                        Object obj = local.get(i);
                        if (obj instanceof String)
                            acceptNullable(visitor, new AsmGenericListReference(this, local,
                                    i, AsmSingleTypeReference.REF_REFONLY, true));
                    }
                }
                {
                    final List<Object> stack = qins.stack;
                    if (stack != null) for (int i = 0; i < stack.size(); i++) {
                        Object obj = stack.get(i);
                        if (obj instanceof String)
                            acceptNullable(visitor, new AsmGenericListReference(this, stack,
                                    i, AsmGenericListReference.REF_REFONLY, true));
                    }
                }
                break;
            }
            case AbstractInsnNode.INVOKE_DYNAMIC_INSN:
                throw new UnrecognizedClassException("FUCK YOU", this, insn);
        }
    }

    protected void yieldFieldReferences(Reference.Visitor visitor, FieldNode node) throws Reference.Visitor.Stop {
        yieldRawFieldSignatureReferences(visitor, node);
        yieldFieldSignatureReferences(visitor, node);
        yieldAnnotationReferences(visitor, node.invisibleAnnotations);
        yieldAnnotationReferences(visitor, node.invisibleTypeAnnotations);
        yieldAnnotationReferences(visitor, node.visibleAnnotations);
        yieldAnnotationReferences(visitor, node.visibleTypeAnnotations);
    }

    protected void yieldFieldSignatureReferences(Reference.Visitor visitor, FieldNode node) throws Reference.Visitor.Stop {
        new AsmFieldDescriptor(this, true, node).parseAndAccept(visitor);
    }

    protected void yieldRawFieldSignatureReferences(Reference.Visitor visitor, FieldNode node) throws Reference.Visitor.Stop {
        //new AsmRawFieldDescriptor(this, true, node).parseAndAccept(visitor);
        acceptNullable(visitor, new AsmFieldReference(this, node, true));
    }

    public ClassNode getResult() {
        while (true) {
            AsmDescriptor desc = dirtyDescs.poll();
            if (desc == null)
                break;
            // If the following fails, the dirty bit does not go away,
            // so it would be re-added to the queue next time.
            // No worries that subsequent calls to getResult()
            // won't attempt to re-synchronize failed descriptors again,
            // since they are gonna fail anyway.
            desc.sync();
        }
        return classNode;
    }

    @Override
    public Object getOriginalIdentifier() {
        return originalIdentifier;
    }

    @Override
    public String getName() {
        // return getResult().name
        return classNode.name;
    }

    @Override
    public String toString() {
        Object origId = getOriginalIdentifier();
        String newName = classNode.name;
        return origId != null && origId.equals(newName)
                ? "<AsmItem name=" + newName + ">"
                : "<AsmItem name=" + origId + " (" + newName + ")>";
    }
}
