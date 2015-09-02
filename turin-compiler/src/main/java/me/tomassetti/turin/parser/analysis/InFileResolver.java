package me.tomassetti.turin.parser.analysis;

import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.implicit.BasicTypes;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by federico on 29/08/15.
 */
public class InFileResolver implements Resolver {

    @Override
    public PropertyDefinition findDefinition(PropertyReference propertyReference) {
        return findDefinitionIn(propertyReference, propertyReference.getParent());
    }

    private PropertyDefinition findDefinitionIn(PropertyReference propertyReference, Node context) {
        for (Node child : context.getChildren()) {
            if (child instanceof PropertyDefinition) {
                PropertyDefinition propertyDefinition = (PropertyDefinition)child;
                if (propertyDefinition.getName().equals(propertyReference.getName())) {
                    return propertyDefinition;
                }
            }
        }
        if (context.getParent() == null) {
            throw new Unresolved(propertyReference);
        }
        return findDefinitionIn(propertyReference, context.getParent());
    }

    @Override
    public TypeDefinition findTypeDefinitionIn(String typeName, Node context) {
        return findTypeDefinitionInHelper(typeName, context, context);
    }

    @Override
    public JvmMethodDefinition findJvmDefinition(FunctionCall functionCall) {
        List<JvmType> argsTypes = functionCall.getActualParamValuesInOrder().stream().map((ap)->ap.calcType(this).jvmType(this)).collect(Collectors.toList());
        Expression function = functionCall.getFunction();
        boolean staticContext = function.isType(this);
        return function.findMethodFor(argsTypes, this, staticContext);
        /*if (functionCall.getFunction().equals("print")) {
            JvmMethodDefinition jvmMethodDefinition = new JvmMethodDefinition("java/lang/System", "out", "Ljava/io/PrintStream;", true);
            jvmMethodDefinition.setStaticField(new JvmStaticFieldDefinition("java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
            return jvmMethodDefinition;
        }*/
        //throw new UnsupportedOperationException(functionCall.toString());
    }

    private TypeDefinition findTypeDefinitionInHelper(String typeName, Node context, Node startContext) {
        if (typeName.startsWith(".")) {
            throw new IllegalArgumentException(typeName);
        }
        for (Node child : context.getChildren()) {
            if (child instanceof TypeDefinition) {
                TypeDefinition typeDefinition = (TypeDefinition)child;
                if (typeDefinition.getName().equals(typeName)) {
                    return typeDefinition;
                }
            }
        }
        if (context.getParent() == null) {
            Optional<TypeDefinition> basicType = BasicTypes.getBasicType(typeName);
            if (basicType.isPresent()) {
                return basicType.get();
            } else {
                return resolveAbsoluteTypeName(typeName, startContext);
            }
        }
        return findTypeDefinitionInHelper(typeName, context.getParent(), startContext);
    }

    private TypeDefinition resolveAbsoluteTypeName(String typeName, Node startContext) {
        if (typeName.equals("java.lang.String") || typeName.equals("java.lang.System") || typeName.equals("java.io.PrintStream")) {
            return ReflectionTypeDefinitionFactory.getInstance().getTypeDefinition(typeName);
        }
        throw new UnresolvedType(typeName, startContext);
    }

}
