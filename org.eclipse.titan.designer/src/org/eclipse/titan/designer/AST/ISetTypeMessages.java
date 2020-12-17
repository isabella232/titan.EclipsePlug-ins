package org.eclipse.titan.designer.AST;

public interface ISetTypeMessages {

	String INCOMPLETEPRESENTERROR = "Not used symbol `-' is not allowed in this context";
	String UNSUPPORTED_FIELDNAME =
			"Sorry, but it is not supported for set types to have a field with a name (`{0}'') "
					+ "which exactly matches the name of the type definition.";
	String NONEMPTYEXPECTED = "A non-empty value was expected for type `{0}''";

	String VALUELISTNOTATIONERRORASN1 = "Value list notation cannot be used for SET type `{0}''";
	String SETVALUEXPECTEDASN1 = "SET value was expected for type `{0}''";
	String NONEXISTENTFIELDASN1 = "Reference to a non-existent component `{0}'' of SET type `{1}''";
	String DUPLICATEFIELDFIRSTASN1 = "Component `{0}'' is already given here";
	String DUPLICATEFIELDAGAINASN1 = "Duplicated SET component `{0}''";
	String MISSINGFIELDASN1 = "Mandatory component `{0}'' is missing from SET value";

	String VALUELISTNOTATIONERRORTTCN3 = "Value list notation cannot be used for set type `{0}''";
	String SETVALUEXPECTEDTTCN3 = "Set value was expected for type `{0}''";
	String NONEXISTENTFIELDTTCN3 = "Reference to a non-existent field `{0}'' in set value for type `{1}''";
	String DUPLICATEFIELDFIRSTTTCN3 = "Field `{0}'' is already given here";
	String DUPLICATEFIELDAGAINTTCN3 = "Duplicated set field `{0}''";
	String MISSINGFIELDTTCN3 = "Field `{0}'' is missing from set value";

	String VALUELISTNOTATIONNOTALLOWED = "Value list notation is not allowed for set type `{0}''";
	String NONEMPTYSETTEMPLATEEXPECTED = "A non-empty set template was expected for type `{0}''";
	String TEMPLATENOTALLOWED = "{0} cannot be used for set type `{1}''";
	String LENGTHRESTRICTIONNOTALLOWED = "Length restriction is not allowed for set type `{0}''";
	String DUPLICATETEMPLATEFIELDFIRST = "Duplicate field `{0}'' in template";
	String DUPLICATETEMPLATEFIELDAGAIN = "Field `{0}'' is already given here";
	String NONEXISTENTTEMPLATEFIELDREFERENCE = "Reference to non-existing field `{0}'' in set template for type `{1}''";
	String MISSINGTEMPLATEFIELD = "Field `{0}'' is missing from template for set type `{1}''";

	String NOFFIELDSDONTMATCH = "The number of fields in set/SET types must be the same";
	String BADOPTIONALITY = "The optionality of fields in set/SET types must be the same";
	String NOTCOMPATIBLESETSETOF = "set/SET and set of/SET OF types are compatible only with other set/SET and set of/SET OF types";
	String NOTCOMPATIBLEUNIONANYTYPE = "union/CHOICE/anytype types are compatible only with other union/CHOICE/anytype types";

}
