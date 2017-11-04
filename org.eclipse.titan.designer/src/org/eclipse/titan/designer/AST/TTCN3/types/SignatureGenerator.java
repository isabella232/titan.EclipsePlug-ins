package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for signature types.
 *
 * 
 * @author Kristof Szabados
 * */
public class SignatureGenerator {

	enum signatureParamaterDirection {PAR_IN, PAR_OUT, PAR_INOUT};

	public static class SignatureParameter {
		private signatureParamaterDirection direction;

		/** Java type name of the parameter */
		private String mJavaTypeName;

		/** Java template name of the parameter */
		private String mJavaTemplateName;

		/** Parameter name */
		private String mJavaName;

		public SignatureParameter(final signatureParamaterDirection direction, final String paramType, final String paramTemplate, final String paramName) {
			this.direction = direction;
			mJavaTypeName = paramType;
			mJavaTemplateName = paramTemplate;
			mJavaName = FieldSubReference.getJavaGetterName(paramName);
		}
	}

	public static class SignatureReturnType {
		/** Java type name of the return type */
		private String mJavaTypeName;

		/** Java template name of the return type */
		private String mJavaTemplateName;

		public SignatureReturnType(final String paramType, final String paramTemplate) {
			mJavaTypeName = paramType;
			mJavaTemplateName = paramTemplate;
		}
	}

	public static class SignatureException {
		/** Java type name of the exception */
		private String mJavaTypeName;

		/** Java template name of the exception */
		private String mJavaTemplateName;

		/** The name to be displayed for the user */
		private String mDisplayName;

		public SignatureException(final String paramType, final String paramTemplate, final String displayName) {
			mJavaTypeName = paramType;
			mJavaTemplateName = paramTemplate;
			mDisplayName = displayName;
		}
	}

	public static class SignatureDefinition {
		private String genName;
		private String displayName;
		private ArrayList<SignatureParameter> formalParameters;
		private SignatureReturnType returnType;
		private boolean isNoBlock;
		private ArrayList<SignatureException> signatureExceptions;

		public SignatureDefinition(final String genName, final String displayName, final ArrayList<SignatureParameter> formalParameters, final SignatureReturnType returnType, final boolean isNoBlock, final ArrayList<SignatureException> signatureExceptions) {
			this.genName = genName;
			this.displayName = displayName;
			this.formalParameters = formalParameters;
			this.returnType = returnType;
			this.isNoBlock = isNoBlock;
			this.signatureExceptions = signatureExceptions;
		}
	}

	private SignatureGenerator() {
		// private to disable instantiation
	}

	/**
	 * This function can be used to generate the class of signature types
	 *
	 * defSignatureClasses in compiler2/ttcn3/signature.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the signature definition to generate code for.
	 * */
	public static void generateClasses(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		aData.addBuiltinTypeImport("TitanBoolean");
		if (!def.signatureExceptions.isEmpty() || !def.formalParameters.isEmpty()) {
			aData.addBuiltinTypeImport("Base_Template.template_sel");
		}

		generateCallClass(aData, source, def);

		// FIXME implement MyProc_redirect
		source.append("// FIXME implement MyProc_redirect\n");

		generateReplyClass(aData, source, def);
		// FIXME implement MyProc_reply_redirect
		source.append("// FIXME implement MyProc_reply_redirect\n");

		generateExceptionClass(aData, source, def);
		generateTemplateClass(aData, source, def);
	}

	/**
	 * This function can be used to generate for signature types that class that handles calls.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the signature definition to generate code for.
	 * */
	private static void generateCallClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		source.append(MessageFormat.format("public static class {0}_call '{'\n", def.genName));
		source.append("// in and inout parameters\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("private {0} param_{1};\n", formalPar.mJavaTypeName, formalPar.mJavaName));
			}
		}

		source.append(MessageFormat.format("public {0}_call() '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("param_{0} = new {1}();\n", formalPar.mJavaName, formalPar.mJavaTypeName));
			}
		}
		source.append("}\n");

		source.append(MessageFormat.format("public {0}_call(final {0}_call otherValue) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("param_{0}.assign(otherValue.get{1}());\n", formalPar.mJavaName, formalPar.mJavaName));
			}
		}
		source.append("}\n");

		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("public {0} get{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				source.append(MessageFormat.format("return param_{0};\n", formalPar.mJavaName));
				source.append("}\n");

				source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				source.append(MessageFormat.format("return param_{0};\n", formalPar.mJavaName));
				source.append("}\n");
			}
		}


		source.append("// FIXME implement encode_text\n");
		source.append("// FIXME implement decode_text\n");

		source.append("public void log() {");
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} : '{' \");\n", def.displayName));
		boolean isFirst = true;
		for (int i = 0; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);
			if (formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				if (isFirst) {
					source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} := \");\n", formalPar.mJavaName));
					isFirst = false;
				} else {
					source.append(MessageFormat.format("TtcnLogger.log_event_str(\", {0} := \");\n", formalPar.mJavaName));
				}
				source.append(MessageFormat.format("param_{0}.log();\n", formalPar.mJavaName));
			}
		}
		source.append("TtcnLogger.log_event_str(\" }\");\n");
		source.append("}\n");

		source.append("}\n");
	}

	/**
	 * This function can be used to generate for signature types that class that handles replies.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the signature definition to generate code for.
	 * */
	private static void generateReplyClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		if(!def.isNoBlock) {
			source.append(MessageFormat.format("public static class {0}_reply '{'\n", def.genName));
			source.append("// out parameters\n");
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("private {0} param_{1};\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("// the reply value of the signature\n");
				source.append(MessageFormat.format("private {0} reply_value;\n", def.returnType.mJavaTypeName));
			}

			source.append(MessageFormat.format("public {0}_reply() '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("param_{0} = new {1}();\n", formalPar.mJavaName, formalPar.mJavaTypeName));
				}
			}
			if (def.returnType != null) {
				source.append(MessageFormat.format("reply_value = new {0}();\n", def.returnType.mJavaTypeName));
			}
			source.append("}\n");

			source.append(MessageFormat.format("public {0}_reply(final {0}_reply other_value) '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("param_{0}.assign(other_value.get{1}());\n", formalPar.mJavaName, formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("reply_value.assign(other_value.getreturn_value());\n");
			}
			source.append("}\n");

			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("public {0} get{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
					source.append(MessageFormat.format("return param_{0};\n", formalPar.mJavaName));
					source.append("}\n");

					source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
					source.append(MessageFormat.format("return param_{0};\n", formalPar.mJavaName));
					source.append("}\n");
				}
			}

			if (def.returnType != null) {
				source.append(MessageFormat.format("public {0} getreturn_value() '{'\n", def.returnType.mJavaTypeName));
				source.append("return reply_value;\n");
				source.append("}\n");

				source.append(MessageFormat.format("public {0} constGetreturn_value() '{'\n", def.returnType.mJavaTypeName));
				source.append("return reply_value;\n");
				source.append("}\n");
			}

			source.append("// FIXME implement encode_text\n");
			source.append("// FIXME implement decode_text\n");

			source.append("public void log() {\n");
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} : '{' \");\n", def.displayName));
			boolean isFirst = true;
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					if (isFirst) {
						source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} := \");\n", formalPar.mJavaName));
						isFirst = false;
					} else {
						source.append(MessageFormat.format("TtcnLogger.log_event_str(\", {0} := \");\n", formalPar.mJavaName));
					}
					source.append(MessageFormat.format("param_{0}.log();\n", formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("TtcnLogger.log_event_str(\" } value \");\n");
				source.append("reply_value.log();\n");
			} else {
				source.append("TtcnLogger.log_event_str(\" }\");\n");
			}
			source.append("}\n");

			source.append("}\n");
		}
	}

	/**
	 * This function can be used to generate for signature types that class that handles exceptions.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the signature definition to generate code for.
	 * */
	private static void generateExceptionClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		if (!def.signatureExceptions.isEmpty()) {
			source.append(MessageFormat.format("public static class {0}_exception '{'\n", def.genName));
			source.append("public enum exception_selection_type {");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);
				source.append(MessageFormat.format(" ALT_{0},", exception.mJavaTypeName));
			}
			source.append(" UNBOUND_VALUE };\n");

			source.append("private exception_selection_type exception_selection;\n");
			source.append("//originally a union which can not be mapped to Java\n");
			source.append("private Base_Type field;\n");

			source.append("//originally clean_up\n");
			source.append("public void cleanUp() {\n");
			source.append("field = null;\n");
			source.append("exception_selection = exception_selection_type.UNBOUND_VALUE;\n");
			source.append("}\n");

			source.append(MessageFormat.format("private void copy_value(final {0}_exception otherValue) '{'\n", def.genName));
			source.append("switch(otherValue.exception_selection){\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);
				source.append(MessageFormat.format("case ALT_{0}:\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("field = new {0}(({0})otherValue.field);\n", exception.mJavaTypeName));
				source.append("break;\n");
			}
				source.append("default:\n");
				source.append(MessageFormat.format("throw new TtcnError(\"Copying an uninitialized exception of signature {0}.\");\n", def.displayName));
				source.append("}\n");
				source.append("exception_selection = otherValue.exception_selection;\n");
			source.append("}\n");

			source.append(MessageFormat.format("public {0}_exception() '{'\n", def.genName));
			source.append("exception_selection = exception_selection_type.UNBOUND_VALUE;\n");
			source.append("}\n");

			source.append(MessageFormat.format("public {0}_exception(final {0}_exception otherValue)  '{'\n", def.genName));
			source.append("copy_value(otherValue);\n");
			source.append("}\n");

			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);
				source.append(MessageFormat.format("public {0}_exception( final {1} otherValue) '{'\n", def.genName, exception.mJavaTypeName));
				source.append(MessageFormat.format("field = new {0}(otherValue);\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("exception_selection = exception_selection_type.ALT_{0};\n", exception.mJavaTypeName));
				source.append("}\n");

				source.append(MessageFormat.format("public {0}_exception( final {1}_template otherValue) '{'\n", def.genName, exception.mJavaTypeName));
				source.append(MessageFormat.format("field = new {0}(otherValue.valueOf());\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("exception_selection = exception_selection_type.ALT_{0};\n", exception.mJavaTypeName));
				source.append("}\n");
			}

			source.append("//originally operator=\n");
			source.append(MessageFormat.format("public {0}_exception assign( final {0}_exception otherValue ) '{'\n", def.genName));
			source.append("if(this != otherValue) {\n");
			source.append("cleanUp();\n");
			source.append("copy_value(otherValue);\n");
			source.append("}\n");
			source.append("return this;\n");
			source.append("}\n");

			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append("//originally {0}_field\n");
				source.append(MessageFormat.format("public {0} get{0}() '{'\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("if (exception_selection != exception_selection_type.ALT_{0}) '{'\n", exception.mJavaTypeName));
				source.append("cleanUp();\n");
				source.append(MessageFormat.format("field = new {0}();\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("exception_selection = exception_selection_type.ALT_{0};\n", exception.mJavaTypeName));
				source.append("}\n");
				source.append(MessageFormat.format("return ({0})field;\n", exception.mJavaTypeName));
				source.append("}\n");

				source.append("//originally const {0}_field\n");
				source.append(MessageFormat.format("public {0} constGet{0}() '{'\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("if (exception_selection != exception_selection_type.ALT_{0}) '{'\n", exception.mJavaTypeName));

				source.append(MessageFormat.format("throw new TtcnError(\"Referencing to non-selected type integer in an exception of signature {0}.\");\n", def.displayName));
				source.append("}\n");
				source.append(MessageFormat.format("return ({0})field;\n", exception.mJavaTypeName));
				source.append("}\n");
			}

			source.append("public exception_selection_type get_selection() {\n");
			source.append("return exception_selection;\n");
			source.append("}\n");

			source.append("public void log() {\n");
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}, \");\n", def.displayName));
			source.append("switch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("case ALT_{0}:\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} : \");\n", exception.mDisplayName));
				source.append("field.log();\n");
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append("TtcnLogger.log_event_str(\"<uninitialized exception>\");\n");
			source.append("}\n");
			source.append("}\n");

			source.append("// FIXME implement encode_text\n");
			source.append("// FIXME implement decode_text\n");
			source.append("}\n");

			source.append(MessageFormat.format("public static class {0}_exception_template '{'\n", def.genName));
			source.append(MessageFormat.format("private {0}_exception.exception_selection_type exception_selection;\n", def.genName));
			source.append("//originally a union which can not be mapped to Java\n");
			source.append("private Base_Template field;\n");

			source.append("//FIXME add support for redirection\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("public {0}_exception_template(final {1} init_template) '{'\n", def.genName, exception.mJavaTemplateName));
				source.append(MessageFormat.format("exception_selection = {0}_exception.exception_selection_type.ALT_{1};\n", def.genName, exception.mJavaTypeName));
				source.append(MessageFormat.format("field = new {0}(init_template);\n", exception.mJavaTemplateName));
				source.append("}\n");
			}

			source.append(MessageFormat.format("public boolean match(final {0}_exception other_value, boolean legacy) '{'\n", def.genName));
			source.append("if (exception_selection != other_value.get_selection()) {\n");
			source.append("return false;\n");
			source.append("}\n");

			source.append("switch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("case ALT_{0}:\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("return (({0}) field).match(other_value.get{1}(), legacy);\n", exception.mJavaTemplateName, exception.mJavaTypeName));
			}
			source.append("default:\n");
			source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector when matching an exception of signature {0}.\");\n", def.displayName));
			source.append("}\n");
			source.append("}\n");

			source.append(MessageFormat.format("public boolean log_match(final {0}_exception other_value, final boolean legacy) '{'\n", def.genName));
			source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0}, \");\n", def.displayName));
			source.append("if (exception_selection == other_value.get_selection()) {\n");
			source.append("switch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("case ALT_{0}:\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} : \");\n", exception.mDisplayName));
				source.append(MessageFormat.format("field.log_match(other_value.constGet{0}(), legacy);\n", exception.mJavaTypeName));
				source.append("break;");
			}
			source.append("default:\n");
			source.append("TtcnLogger.log_event_str(\"<invalid selector>\");");
			source.append("}\n");
			source.append("} else {\n");
			source.append("other_value.log();\n");
			source.append("TtcnLogger.log_event_str(\" with \");\n");
			source.append("switch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("case ALT_{0}:\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\"{0} : \");\n", exception.mDisplayName));
				source.append("field.log();\n");
				source.append("break;");
			}
			source.append("default:\n");
			source.append("TtcnLogger.log_event_str(\"<invalid selector>\");");
			source.append("}\n");
			source.append("if (match(other_value, legacy)) TtcnLogger.log_event_str(\" matched\");\n");
			source.append("else TtcnLogger.log_event_str(\" unmatched\");\n");
			source.append("}\n");
			source.append("}\n");

			source.append("// FIXME implement set_value\n");

			source.append("public boolean is_any_or_omit() {\n");
			source.append("switch(exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("case ALT_{0}:\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("return (({0}) field).getSelection() == template_sel.ANY_OR_OMIT;\n", exception.mJavaTemplateName));
			}
			source.append("default:\n");
			source.append("break;\n");
			source.append("}\n");

			source.append(MessageFormat.format("throw new TtcnError(\"Internal error: Invalid selector when checking for '*' in an exception template of signature {0}.\");\n", def.displayName));
			source.append("}\n");
			source.append("}\n");
		}
	}

	/**
	 * This function can be used to generate for signature types the template class.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param def the signature definition to generate code for.
	 * */
	private static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		source.append(MessageFormat.format("public static class {0}_template '{'\n", def.genName));
		source.append("// all the parameters\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("private {0} param_{1};\n", formalPar.mJavaTemplateName, formalPar.mJavaName));
		}
		if (def.returnType != null) {
			source.append(MessageFormat.format("private {0} reply_value;\n", def.returnType.mJavaTemplateName));
		}

		source.append(MessageFormat.format("public {0}_template() '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("param_{0} = new {1}(template_sel.ANY_VALUE);\n", formalPar.mJavaName, formalPar.mJavaTemplateName));
		}
		source.append("}\n\n");
		source.append(MessageFormat.format("public {0}_template(final {0}_template otherValue) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("param_{0} = new {1}(otherValue.get{0}());\n", formalPar.mJavaName, formalPar.mJavaTemplateName));
		}
		source.append("}\n\n");

		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("public {0} get{1}() '{'\n", formalPar.mJavaTemplateName, formalPar.mJavaName ));
			source.append(MessageFormat.format("return param_{0};\n", formalPar.mJavaName ));
			source.append("}\n");

			source.append(MessageFormat.format("public {0} constGet{1}() '{'\n", formalPar.mJavaTemplateName, formalPar.mJavaName ));
			source.append(MessageFormat.format("return param_{0};\n", formalPar.mJavaName ));
			source.append("}\n");
		}

		if (def.returnType != null) {
			source.append(MessageFormat.format("public {0} return_value() '{'\n", def.returnType.mJavaTemplateName));
			source.append("return reply_value;\n");
			source.append("}\n");
		}

		source.append(MessageFormat.format("public {0}_call create_call() '{'\n", def.genName));
		source.append(MessageFormat.format("{0}_call return_value = new {0}_call();\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("return_value.get{0}().assign(param_{0}.valueOf());\n", formalPar.mJavaName ));
			}
		}
		source.append("return return_value;\n");
		source.append("}\n");

		if(!def.isNoBlock) {
			source.append(MessageFormat.format("public {0}_reply create_reply() '{'\n", def.genName));
			source.append(MessageFormat.format("{0}_reply return_value = new {0}_reply();\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("return_value.get{0}().assign(param_{0}.valueOf());\n", formalPar.mJavaName ));
				}
			}
			source.append("return return_value;\n");
			source.append("}\n");
		}

		source.append(MessageFormat.format("public boolean match_call(final {0}_call match_value) '{'\n", def.genName));
		source.append("return match_call(match_value, false);\n");
		source.append("}\n");

		source.append(MessageFormat.format("public boolean match_call(final {0}_call match_value, final boolean legacy) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("if (!param_{0}.match(match_value.get{0}(), legacy)) '{'return false;'}'\n", formalPar.mJavaName ));
			}
		}
		source.append("return true;\n");
		source.append("}\n");

		if(!def.isNoBlock) {
			source.append(MessageFormat.format("public boolean match_reply(final {0}_reply match_value) '{'\n", def.genName));
			source.append("return match_reply(match_value, false);\n");
			source.append("}\n");

			source.append(MessageFormat.format("public boolean match_reply(final {0}_reply match_value, final boolean legacy) '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("if (!param_{0}.match(match_value.get{0}(), legacy)) '{'return false;'}'\n", formalPar.mJavaName ));
				}
			}
			if (def.returnType != null) {
				source.append("if (!reply_value.match(match_value.getreturn_value(), legacy)) {return false;}\n");
			}
			source.append("return true;\n");
			source.append("}\n");
		}

		if (def.returnType != null) {
			source.append(MessageFormat.format("public {0}_template set_value_template(final {1} new_template) '{'\n", def.genName, def.returnType.mJavaTypeName));
			source.append(MessageFormat.format("reply_value = new {0}(new_template);\n", def.returnType.mJavaTemplateName));
			source.append("return this;\n");
			source.append("}\n");
			source.append(MessageFormat.format("public {0}_template set_value_template(final {1} new_template) '{'\n", def.genName, def.returnType.mJavaTemplateName));
			source.append("reply_value = new_template;\n");
			source.append("return this;\n");
			source.append("}\n");
		}

		source.append("// FIXME implement encode_text\n");
		source.append("// FIXME implement decode_text\n");
	
		source.append("public void log() {\n");
		boolean isFirst = true;
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if (isFirst) {
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\"'{' {0} := \");\n", formalPar.mJavaName));
				isFirst = false;
			} else {
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\", {0} := \");\n", formalPar.mJavaName));
			}
			source.append(MessageFormat.format("param_{0}.log();\n", formalPar.mJavaName ));
		}
		source.append("TtcnLogger.log_event_str(\" }\");\n");
		source.append("}\n");

		source.append(MessageFormat.format("public void log_match_call(final {0}_call match_value, final boolean legacy) '{'\n", def.genName));
		isFirst = true;
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				if (isFirst) {
					source.append(MessageFormat.format("TtcnLogger.log_event_str(\"'{' {0} := \");\n", formalPar.mJavaName ));
					isFirst = false;
				} else {
					source.append(MessageFormat.format("TtcnLogger.log_event_str(\", {0} := \");\n", formalPar.mJavaName ));
				}
				source.append(MessageFormat.format("param_{0}.log_match(match_value.get{0}(), legacy);\n", formalPar.mJavaName ));
			}
		}
		source.append("TtcnLogger.log_event_str(\" }\");\n");
		source.append("}\n");

		source.append(MessageFormat.format("public void log_match_reply(final {0}_reply match_value, final boolean legacy) '{'\n", def.genName));
		isFirst = true;
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
				if (isFirst) {
					source.append(MessageFormat.format("TtcnLogger.log_event_str(\"'{' {0} := \");\n", formalPar.mJavaName ));
					isFirst = false;
				} else {
					source.append(MessageFormat.format("TtcnLogger.log_event_str(\", {0} := \");\n", formalPar.mJavaName ));
				}
				source.append(MessageFormat.format("param_{0}.log_match(match_value.get{0}(), legacy);\n", formalPar.mJavaName ));
			}
		}
		if (def.returnType != null) {
			source.append("TtcnLogger.log_event_str(\" } value \");\n");
			source.append("reply_value.log_match(match_value.getreturn_value(), legacy);\n");
		}
		source.append("}\n");
		
		source.append("}\n");
	}
}
