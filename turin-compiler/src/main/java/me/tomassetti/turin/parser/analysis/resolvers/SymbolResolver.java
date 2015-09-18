package me.tomassetti.turin.parser.analysis.resolvers;

import me.tomassetti.turin.jvm.JvmMethodDefinition;
import me.tomassetti.turin.parser.analysis.UnsolvedSymbolException;
import me.tomassetti.turin.parser.ast.*;
import me.tomassetti.turin.parser.ast.expressions.FunctionCall;
import me.tomassetti.turin.parser.ast.typeusage.TypeUsage;

import java.util.Optional;

/**
 * Resolve symbols potentially consider the local context.
 */
public interface SymbolResolver {

    /**
     * Given a PropertyReference it finds the corresponding declaration.
     */
    Optional<PropertyDefinition> findDefinition(PropertyReference propertyReference);

    /**
     * Find the TypeDefinition corresponding to the given name in the given context.
     *
     * @param typeName can be a simple name or a canonical name. Note that is not legal to pass a primitive type name
     *                 because it is not a valid identifier and there are no TypeDefinition associated
     * @param resolver top level resolver used during compilation. This is needed because this resolver could delegate
     *                 to that one during the resolution process.
     */
    default TypeDefinition getTypeDefinitionIn(String typeName, Node context, SymbolResolver resolver) {
        Optional<TypeDefinition> result = findTypeDefinitionIn(typeName, context, resolver);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new UnsolvedSymbolException(context, typeName);
        }
    }

    /**
     * @param typeName can be a simple name or a canonical name. Note that is not legal to pass a primitive type name
     *                 because it is not a valid identifier and there are no TypeDefinition associated
     * @param resolver top level resolver used during compilation. This is needed because this resolver could delegate
     *                 to that one during the resolution process.
     */
    Optional<TypeDefinition> findTypeDefinitionIn(String typeName, Node context, SymbolResolver resolver);

    /**
     * @param typeName can be a simple name or a canonical name. It is legal to pass a primitive type name.
     * @param resolver top level resolver used during compilation. This is needed because this resolver could delegate
     *                 to that one during the resolution process.
     */
    Optional<TypeUsage> findTypeUsageIn(String typeName, Node context, SymbolResolver resolver);

    /**
     * Find the JVM method corresponding to this function call.
     */
    JvmMethodDefinition findJvmDefinition(FunctionCall functionCall);

    /**
     * Find whatever Node is corresponding to the given name in the given context.
     */
    Optional<Node> findSymbol(String name, Node context);
}