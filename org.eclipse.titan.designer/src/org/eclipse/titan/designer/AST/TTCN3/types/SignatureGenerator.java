/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.compiler.JavaGenData;

/**
 * Utility class for generating the value and template classes for signature
 * types.
 *
 *
 * @author Kristof Szabados
 * */
public final class SignatureGenerator {

	enum signatureParamaterDirection {PAR_IN, PAR_OUT, PAR_INOUT};

	public static class SignatureParameter {
		private final signatureParamaterDirection direction;

		/** Java type name of the parameter */
		private final String mJavaTypeName;

		/** Java template name of the parameter */
		private final String mJavaTemplateName;

		/** Parameter name */
		private final String mJavaName;

		public SignatureParameter(final signatureParamaterDirection direction, final String paramType, final String paramTemplate, final String paramName) {
			this.direction = direction;
			mJavaTypeName = paramType;
			mJavaTemplateName = paramTemplate;
			mJavaName = FieldSubReference.getJavaGetterName(paramName);
		}
	}

	public static class SignatureReturnType {
		/** Java type name of the return type */
		private final String mJavaTypeName;

		/** Java template name of the return type */
		private final String mJavaTemplateName;

		public SignatureReturnType(final String paramType, final String paramTemplate) {
			mJavaTypeName = paramType;
			mJavaTemplateName = paramTemplate;
		}
	}

	public static class SignatureException {
		/** Java type name of the exception */
		private final String mJavaTypeName;

		/** the name of the selection enum */
		private final String mJavaSelectionName;

		/** Java template name of the exception */
		private final String mJavaTemplateName;

		/** The name to be displayed for the user */
		private final String mDisplayName;

		public SignatureException(final String paramType, final String paramTemplate, final String displayName) {
			mJavaTypeName = paramType;
			mJavaSelectionName = FieldSubReference.getJavaGetterName( paramType );//paramType.replace('.', '_');
			mJavaTemplateName = paramTemplate;
			mDisplayName = displayName;
		}
	}

	public static class SignatureDefinition {
		private final String genName;
		private final String displayName;
		private final ArrayList<SignatureParameter> formalParameters;
		private final SignatureReturnType returnType;
		private final boolean isNoBlock;
		private final ArrayList<SignatureException> signatureExceptions;

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
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	public static void generateClasses(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		if (!def.signatureExceptions.isEmpty() || !def.formalParameters.isEmpty()) {
			aData.addBuiltinTypeImport("Base_Template.template_sel");
		}
		if (def.formalParameters.isEmpty()) {
			aData.addBuiltinTypeImport("TitanNull_Type");
		}

		generateCallClass(aData, source, def);
		generateRedirectClass(aData, source, def);
		generateReplyClass(aData, source, def);
		generateReplyRedirectClass(aData, source, def);
		generateExceptionClass(aData, source, def);
		generateTemplateClass(aData, source, def);
	}

	/**
	 * This function can be used to generate for signature types that class
	 * that handles calls.
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	private static void generateCallClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		source.append(MessageFormat.format("\tpublic static class {0}_call '{'\n", def.genName));
		source.append("\t\t// in and inout parameters\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\tprivate {0} param_{1};\n", formalPar.mJavaTypeName, formalPar.mJavaName));
			}
		}

		source.append(MessageFormat.format("\t\tpublic {0}_call() '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tparam_{0} = new {1}();\n", formalPar.mJavaName, formalPar.mJavaTypeName));
			}
		}
		source.append("\t\t}\n");

		source.append(MessageFormat.format("\t\tpublic {0}_call(final {0}_call otherValue) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tparam_{0} = new {1}(otherValue.get_field_{2}());\n", formalPar.mJavaName, formalPar.mJavaTypeName, formalPar.mJavaName));
			}
		}
		source.append("\t\t}\n");

		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\tpublic {0} get_field_{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				source.append(MessageFormat.format("\t\t\treturn param_{0};\n", formalPar.mJavaName));
				source.append("\t\t}\n");

				source.append(MessageFormat.format("\t\tpublic {0} constGet_field_{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				source.append(MessageFormat.format("\t\t\treturn param_{0};\n", formalPar.mJavaName));
				source.append("\t\t}\n");
			}
		}

		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tparam_{0}.encode_text(text_buf);\n", formalPar.mJavaName));
			}
		}
		source.append("\t\t}\n");

		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tparam_{0}.decode_text(text_buf);\n", formalPar.mJavaName));
			}
		}
		source.append("\t\t}\n\n");

		source.append("\t\t/**\n");
		source.append("\t\t * Logs this value.\n");
		source.append("\t\t */\n");
		source.append("\t\tpublic void log() {\n");
		source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"{0} : '{' \");\n", def.displayName));
		boolean isFirst = true;
		for (int i = 0; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);
			if (formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				if (isFirst) {
					source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"{0} := \");\n", formalPar.mJavaName));
					isFirst = false;
				} else {
					source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\", {0} := \");\n", formalPar.mJavaName));
				}
				source.append(MessageFormat.format("\t\t\tparam_{0}.log();\n", formalPar.mJavaName));
			}
		}
		source.append("\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		source.append("\t\t}\n");

		source.append("\t}\n\n");
	}

	/**
	 * This function can be used to generate for signature types that class
	 * that handles redirections.
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	private static void generateRedirectClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		source.append(MessageFormat.format("\tpublic static class {0}_call_redirect '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\tprivate {0} ptr_{1};\n", formalPar.mJavaTypeName, formalPar.mJavaName));
			}
		}

		source.append(MessageFormat.format("\t\tpublic {0}_call_redirect( ) '{'", def.genName));
		source.append("\t\t}\n");

		boolean longConstructorNeeded = false;
		for (int i = 0 ; i < def.formalParameters.size() && !longConstructorNeeded; i++) {
			if (def.formalParameters.get(i).direction != signatureParamaterDirection.PAR_OUT) {
				longConstructorNeeded = true;
			}
		}

		if (longConstructorNeeded) {
			source.append(MessageFormat.format("\t\tpublic {0}_call_redirect( ", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);
				if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
					if (i != 0) {
						source.append(", ");
					}

					source.append(MessageFormat.format("final {0} par_{1}", formalPar.mJavaTypeName, formalPar.mJavaName));
				}
			}
			source.append(" ) {\n");
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);
				if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
					source.append(MessageFormat.format("\t\t\tptr_{0} = par_{0};\n", formalPar.mJavaName));
				}
			}
			source.append("\t\t}\n");
		}

		source.append(MessageFormat.format("\t\tpublic void set_parameters( final {0}_call call_par) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tif (ptr_{0} != null) '{'\n", formalPar.mJavaName));
				source.append(MessageFormat.format("\t\t\t\tptr_{0}.operator_assign(call_par.constGet_field_{0}());\n", formalPar.mJavaName));
				source.append("\t\t\t}\n");
			}
		}
		source.append("\t\t}\n");
		source.append("\t}\n\n");
	}

	/**
	 * This function can be used to generate for signature types that class
	 * that handles replies.
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	private static void generateReplyClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		if(!def.isNoBlock) {
			source.append(MessageFormat.format("\tpublic static class {0}_reply '{'\n", def.genName));
			source.append("\t\t// out parameters\n");
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\tprivate {0} param_{1};\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("\t\t// the reply value of the signature\n");
				source.append(MessageFormat.format("\t\tprivate {0} reply_value;\n", def.returnType.mJavaTypeName));
			}

			source.append(MessageFormat.format("\t\tpublic {0}_reply() '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\tparam_{0} = new {1}();\n", formalPar.mJavaName, formalPar.mJavaTypeName));
				}
			}
			if (def.returnType != null) {
				source.append(MessageFormat.format("\t\t\treply_value = new {0}();\n", def.returnType.mJavaTypeName));
			}
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tpublic {0}_reply(final {0}_reply other_value) '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\tparam_{0} = new {1}(other_value.get_field_{2}());\n", formalPar.mJavaName, formalPar.mJavaTypeName, formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append(MessageFormat.format("\t\t\treply_value = new {0}(other_value.get_return_value());\n", def.returnType.mJavaTypeName));
			}
			source.append("\t\t}\n");

			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\tpublic {0} get_field_{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
					source.append(MessageFormat.format("\t\t\treturn param_{0};\n", formalPar.mJavaName));
					source.append("\t\t}\n");

					source.append(MessageFormat.format("\t\tpublic {0} constGet_field_{1}() '{'\n", formalPar.mJavaTypeName, formalPar.mJavaName));
					source.append(MessageFormat.format("\t\t\treturn param_{0};\n", formalPar.mJavaName));
					source.append("\t\t}\n");
				}
			}

			if (def.returnType != null) {
				source.append(MessageFormat.format("\t\tpublic {0} get_return_value() '{'\n", def.returnType.mJavaTypeName));
				source.append("\t\t\treturn reply_value;\n");
				source.append("\t\t}\n");

				source.append(MessageFormat.format("\t\tpublic {0} constGet_return_value() '{'\n", def.returnType.mJavaTypeName));
				source.append("\t\t\treturn reply_value;\n");
				source.append("\t\t}\n");
			}

			source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\tparam_{0}.encode_text(text_buf);\n", formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("\t\t\treply_value.encode_text(text_buf);\n");
			}
			source.append("\t\t}\n");

			source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\tparam_{0}.decode_text(text_buf);\n", formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("\t\t\treply_value.decode_text(text_buf);\n");
			}
			source.append("\t\t}\n\n");

			source.append("\t\t/**\n");
			source.append("\t\t * Logs this value.\n");
			source.append("\t\t */\n");
			source.append("\t\tpublic void log() {\n");
			source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"{0} : '{' \");\n", def.displayName));
			boolean isFirst = true;
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					if (isFirst) {
						source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"{0} := \");\n", formalPar.mJavaName));
						isFirst = false;
					} else {
						source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\", {0} := \");\n", formalPar.mJavaName));
					}
					source.append(MessageFormat.format("\t\t\tparam_{0}.log();\n", formalPar.mJavaName));
				}
			}
			if (def.returnType != null) {
				source.append("\t\t\tTTCN_Logger.log_event_str(\" } value \");\n");
				source.append("\t\t\treply_value.log();\n");
			} else {
				source.append("\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
			}
			source.append("\t\t}\n");

			source.append("\t}\n\n");
		}
	}

	/**
	 * This function can be used to generate for signature types that class
	 * that handles reply redirections.
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	private static void generateReplyRedirectClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		if(!def.isNoBlock) {
			source.append(MessageFormat.format("\tpublic static class {0}_reply_redirect '{'\n", def.genName));
			if (def.returnType != null) {
				aData.addBuiltinTypeImport("Value_Redirect_Interface");

				source.append("\t\t// the reply value of the signature\n");
				source.append("\t\tprivate Value_Redirect_Interface ret_val_redir;\n");
			}
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\tprivate {0} ptr_{1};\n", formalPar.mJavaTypeName, formalPar.mJavaName));
				}
			}

			source.append(MessageFormat.format("\t\tpublic {0}_reply_redirect( ", def.genName));
			if (def.returnType != null) {
				source.append("final Value_Redirect_Interface return_redir");
			}
			source.append(" ) {\n");
			if (def.returnType != null) {
				source.append(MessageFormat.format("\t\t\tret_val_redir = return_redir;\n", def.returnType.mJavaTypeName));
			}
			source.append("\t\t}\n");

			boolean longConstructorNeeded = false;
			for (int i = 0 ; i < def.formalParameters.size() && !longConstructorNeeded; i++) {
				if (def.formalParameters.get(i).direction != signatureParamaterDirection.PAR_IN) {
					longConstructorNeeded = true;
				}
			}

			if (longConstructorNeeded) {
				source.append(MessageFormat.format("\t\tpublic {0}_reply_redirect( ", def.genName));
				boolean first = true;
				if (def.returnType != null) {
					source.append("final Value_Redirect_Interface return_redir");
					first = false;
				}
				for (int i = 0 ; i < def.formalParameters.size(); i++) {
					final SignatureParameter formalPar = def.formalParameters.get(i);

					if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
						if (!first) {
							source.append(", ");
						}
						source.append(MessageFormat.format("final {0} par_{1}", formalPar.mJavaTypeName, formalPar.mJavaName));
						first = false;
					}
				}
				source.append(" ) {\n");
				if (def.returnType != null) {
					source.append(MessageFormat.format("\t\t\tret_val_redir = return_redir;\n", def.returnType.mJavaTypeName));
				}
				for (int i = 0 ; i < def.formalParameters.size(); i++) {
					final SignatureParameter formalPar = def.formalParameters.get(i);

					if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
						source.append(MessageFormat.format("\t\t\tptr_{0} = par_{0};\n", formalPar.mJavaName));
					}
				}
				source.append("\t\t}\n");
			}

			source.append(MessageFormat.format("\t\tpublic void set_parameters( final {0}_reply reply_par) '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\tif (ptr_{0} != null) '{'\n", formalPar.mJavaName));
					source.append(MessageFormat.format("\t\t\t\tptr_{0}.operator_assign(reply_par.constGet_field_{0}());\n", formalPar.mJavaName));
					source.append("\t\t\t}\n");
				}
			}
			if (def.returnType != null) {
				source.append("\t\t\tif (ret_val_redir != null) {\n");
				source.append(MessageFormat.format("\t\t\t\tret_val_redir.set_values(reply_par.constGet_return_value());\n", def.returnType.mJavaTypeName));
				source.append("\t\t\t}\n");
			}
			source.append("\t\t}\n");
			source.append("\t}\n");
		}
	}

	/**
	 * This function can be used to generate for signature types that class
	 * that handles exceptions.
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	private static void generateExceptionClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		if (!def.signatureExceptions.isEmpty()) {
			aData.addBuiltinTypeImport("Value_Redirect_Interface");

			source.append(MessageFormat.format("\tpublic static class {0}_exception '{'\n", def.genName));
			source.append("\t\tpublic enum exception_selection_type {");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);
				source.append(MessageFormat.format(" ALT_{0},", exception.mJavaSelectionName));
			}
			source.append(" UNBOUND_VALUE };\n");

			source.append("\t\tprivate exception_selection_type exception_selection;\n");
			source.append("\t\t//originally a union which can not be mapped to Java\n");
			source.append("\t\tprivate Base_Type field;\n");

			source.append("\t\t/**\n");
			source.append("\t\t * Deletes the value, setting it to unbound.\n");
			source.append("\t\t *\n");
			source.append("\t\t * clean_up() in the core\n");
			source.append("\t\t * */\n");
			source.append("\t\tpublic void clean_up() {\n");
			source.append("\t\t\tfield = null;\n");
			source.append("\t\t\texception_selection = exception_selection_type.UNBOUND_VALUE;\n");
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tprivate void copy_value(final {0}_exception otherValue) '{'\n", def.genName));
			source.append("\t\t\tswitch (otherValue.exception_selection){\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);
				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\tfield = new {0}(({0})otherValue.field);\n", exception.mJavaTypeName));
				source.append("\t\t\t\tbreak;\n");
			}
				source.append("\t\t\tdefault:\n");
				source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Copying an uninitialized exception of signature {0}.\");\n", def.displayName));
				source.append("\t\t\t}\n");
				source.append("\t\t\texception_selection = otherValue.exception_selection;\n");
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tpublic {0}_exception() '{'\n", def.genName));
			source.append("\t\t\texception_selection = exception_selection_type.UNBOUND_VALUE;\n");
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tpublic {0}_exception(final {0}_exception otherValue)  '{'\n", def.genName));
			source.append("\t\t\tcopy_value(otherValue);\n");
			source.append("\t\t}\n");

			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);
				source.append(MessageFormat.format("\t\tpublic {0}_exception( final {1} otherValue) '{'\n", def.genName, exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\t\tfield = new {0}(otherValue);\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\t\texception_selection = exception_selection_type.ALT_{0};\n", exception.mJavaSelectionName));
				source.append("\t\t}\n");

				source.append(MessageFormat.format("\t\tpublic {0}_exception( final {1}_template otherValue) '{'\n", def.genName, exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\t\tfield = new {0}(otherValue.valueof());\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\t\texception_selection = exception_selection_type.ALT_{0};\n", exception.mJavaSelectionName));
				source.append("\t\t}\n");
			}

			if ( aData.isDebug() ) {
				source.append("\t\t/**\n");
				source.append("\t\t * Assigns the other value to this value.\n");
				source.append("\t\t * Overwriting the current content in the process.\n");
				source.append("\t\t *<p>\n");
				source.append("\t\t * operator= in the core.\n");
				source.append("\t\t *\n");
				source.append("\t\t * @param otherValue\n");
				source.append("\t\t *                the other value to assign.\n");
				source.append("\t\t * @return the new value object.\n");
				source.append("\t\t */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0}_exception operator_assign( final {0}_exception otherValue ) '{'\n", def.genName));
			source.append("\t\t\tif(this != otherValue) {\n");
			source.append("\t\t\t\tclean_up();\n");
			source.append("\t\t\t\tcopy_value(otherValue);\n");
			source.append("\t\t\t}\n");
			source.append("\t\t\treturn this;\n");
			source.append("\t\t}\n");

			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t//originally {0}_field\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\tpublic {0} get_field_{1}() '{'\n", exception.mJavaTypeName, exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\tif (exception_selection != exception_selection_type.ALT_{0}) '{'\n", exception.mJavaSelectionName));
				source.append("\t\t\t\tclean_up();\n");
				source.append(MessageFormat.format("\t\t\t\tfield = new {0}();\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\t\t\texception_selection = exception_selection_type.ALT_{0};\n", exception.mJavaSelectionName));
				source.append("\t\t\t}\n");
				source.append(MessageFormat.format("\t\t\treturn ({0})field;\n", exception.mJavaTypeName));
				source.append("\t\t}\n");

				source.append(MessageFormat.format("\t\t//originally const {0}_field\n", exception.mJavaTypeName));
				source.append(MessageFormat.format("\t\tpublic {0} constGet_field_{1}() '{'\n", exception.mJavaTypeName, exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\tif (exception_selection != exception_selection_type.ALT_{0}) '{'\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Referencing to non-selected type integer in an exception of signature {0}.\");\n", def.displayName));
				source.append("\t\t\t}\n");
				source.append(MessageFormat.format("\t\t\treturn ({0})field;\n", exception.mJavaTypeName));
				source.append("\t\t}\n");
			}

			source.append("\t\tpublic exception_selection_type get_selection() {\n");
			source.append("\t\t\treturn exception_selection;\n");
			source.append("\t\t}\n");

			source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
			source.append("\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\ttext_buf.push_int({0});\n", i));
				source.append("\t\t\t\tfield.encode_text(text_buf);\n");
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text encoder: Encoding an uninitialized exception of signature {0}.\");\n", def.displayName));
			source.append("\t\t\t}\n");
			source.append("\t\t}\n");

			source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
			source.append("\t\t\tfinal TitanInteger temp = text_buf.pull_int();\n");
			source.append("\t\t\tswitch (temp.get_int()) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\tcase {0}:\n", i));
				source.append(MessageFormat.format("\t\t\t\tget_field_{0}().decode_text(text_buf);\n", exception.mJavaSelectionName));
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Text decoder: Unrecognized selector was received for an exception of signature {0}.\");\n", def.displayName));
			source.append("\t\t\t}\n");
			source.append("\t\t}\n\n");

			source.append("\t\t/**\n");
			source.append("\t\t * Logs this value.\n");
			source.append("\t\t */\n");
			source.append("\t\tpublic void log() {\n");
			source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"{0}, \");\n", def.displayName));
			source.append("\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\tTTCN_Logger.log_event_str(\"{0} : \");\n", exception.mDisplayName));
				source.append("\t\t\t\tfield.log();\n");
				source.append("\t\t\t\tbreak;\n");
			}
			source.append("\t\t\tdefault:\n");
			source.append("\t\t\t\tTTCN_Logger.log_event_str(\"<uninitialized exception>\");\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n");

			source.append("\t}\n");

			source.append(MessageFormat.format("\tpublic static class {0}_exception_template '{'\n", def.genName));
			source.append(MessageFormat.format("\t\tprivate {0}_exception.exception_selection_type exception_selection;\n", def.genName));
			source.append("\t\t//originally a union which can not be mapped to Java\n");
			source.append("\t\tprivate Base_Template field;\n");
			source.append("\t\tprivate Value_Redirect_Interface redirection_field;\n");

			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\tpublic {0}_exception_template(final {1} init_template) '{'\n", def.genName, exception.mJavaTemplateName));
				source.append(MessageFormat.format("\t\t\texception_selection = {0}_exception.exception_selection_type.ALT_{1};\n", def.genName, exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\tfield = new {0}(init_template);\n", exception.mJavaTemplateName));
				source.append("\t\t}\n");

				source.append(MessageFormat.format("\t\tpublic {0}_exception_template(final {1} init_template, final Value_Redirect_Interface value_redirect) '{'\n", def.genName, exception.mJavaTemplateName));
				source.append(MessageFormat.format("\t\t\texception_selection = {0}_exception.exception_selection_type.ALT_{1};\n", def.genName, exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\tfield = new {0}(init_template);\n", exception.mJavaTemplateName));
				source.append("\t\t\tredirection_field = value_redirect;\n");
				source.append("\t\t}\n\n");
			}

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append("\t\t * Matches the provided ecpetion value against this template. In legacy mode\n");
				source.append("\t\t * omitted value fields are not matched against the template field.\n");
				source.append("\t\t *\n");
				source.append("\t\t * @param other_value\n");
				source.append("\t\t *                the value to be matched.\n");
				source.append("\t\t * @param legacy\n");
				source.append("\t\t *                use legacy mode.\n");
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic boolean match(final {0}_exception other_value, final boolean legacy) '{'\n", def.genName));
			source.append("\t\t\tif (exception_selection != other_value.get_selection()) {\n");
			source.append("\t\t\t\treturn false;\n");
			source.append("\t\t\t}\n");

			source.append("\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\treturn (({0}) field).match(other_value.get_field_{1}(), legacy);\n", exception.mJavaTemplateName, exception.mJavaSelectionName));
			}
			source.append("\t\t\tdefault:\n");
			source.append(MessageFormat.format("\t\t\t\tthrow new TtcnError(\"Internal error: Invalid selector when matching an exception of signature {0}.\");\n", def.displayName));
			source.append("\t\t\t}\n");
			source.append("\t\t}\n\n");

			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append("\t\t * Logs the matching of the provided value to this template, to help\n");
				source.append("\t\t * identify the reason for mismatch. In legacy mode omitted value fields\n");
				source.append("\t\t * are not matched against the template field.\n");
				source.append("\t\t *\n");
				source.append("\t\t * @param match_value\n");
				source.append("\t\t *                the value to be matched.\n");
				source.append("\t\t * @param legacy\n");
				source.append("\t\t *                use legacy mode.\n");
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic void log_match(final {0}_exception match_value, final boolean legacy) '{'\n", def.genName));
			source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"{0}, \");\n", def.displayName));
			source.append("\t\t\tif (exception_selection == match_value.get_selection()) {\n");
			source.append("\t\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\t\tTTCN_Logger.log_event_str(\"{0} : \");\n", exception.mDisplayName));
				source.append(MessageFormat.format("\t\t\t\t\tfield.log_match(match_value.constGet_field_{0}(), legacy);\n", exception.mJavaSelectionName));
				source.append("\t\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"<invalid selector>\");\n");
			source.append("\t\t\t\t\tbreak;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t} else {\n");
			source.append("\t\t\t\tmatch_value.log();\n");
			source.append("\t\t\t\tTTCN_Logger.log_event_str(\" with \");\n");
			source.append("\t\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\t\tTTCN_Logger.log_event_str(\"{0} : \");\n", exception.mDisplayName));
				source.append("\t\t\t\t\tfield.log();\n");
				source.append("\t\t\t\t\tbreak;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\"<invalid selector>\");\n");
			source.append("\t\t\t\t\tbreak;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t\tif (match(match_value, legacy)) {\n");
			source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" matched\");\n");
			source.append("\t\t\t\t} else {\n");
			source.append("\t\t\t\t\tTTCN_Logger.log_event_str(\" unmatched\");\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tpublic void set_value(final {0}_exception source_value) '{'\n", def.genName));
			source.append("\t\t\tif (exception_selection == source_value.get_selection()) {\n");
			source.append("\t\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append("\t\t\t\t\tif (redirection_field != null) {\n");
				source.append(MessageFormat.format("\t\t\t\t\t\tredirection_field.set_values(source_value.constGet_field_{0}());\n", exception.mJavaSelectionName));
				source.append("\t\t\t\t\t}\n");
				source.append("\t\t\t\t\treturn;\n");
			}
			source.append("\t\t\t\tdefault:\n");
			source.append("\t\t\t\t\tbreak;\n");
			source.append("\t\t\t\t}\n");
			source.append("\t\t\t}\n");
			source.append("\t\t}\n\n");

			source.append("\t\tpublic boolean is_any_or_omit() {\n");
			source.append("\t\t\tswitch (exception_selection) {\n");
			for ( int i = 0; i < def.signatureExceptions.size(); i++) {
				final SignatureException exception = def.signatureExceptions.get(i);

				source.append(MessageFormat.format("\t\t\tcase ALT_{0}:\n", exception.mJavaSelectionName));
				source.append(MessageFormat.format("\t\t\t\treturn (({0}) field).get_selection() == template_sel.ANY_OR_OMIT;\n", exception.mJavaTemplateName));
			}
			source.append("\t\t\tdefault:\n");
			source.append("\t\t\t\tbreak;\n");
			source.append("\t\t\t}\n");

			source.append(MessageFormat.format("\t\t\tthrow new TtcnError(\"Internal error: Invalid selector when checking for '*' in an exception template of signature {0}.\");\n", def.displayName));
			source.append("\t\t}\n");
			source.append("\t}\n");
		}
	}

	/**
	 * This function can be used to generate for signature types the
	 * template class.
	 *
	 * @param aData
	 *                used to access build settings.
	 * @param source
	 *                where the source code is to be generated.
	 * @param def
	 *                the signature definition to generate code for.
	 * */
	private static void generateTemplateClass(final JavaGenData aData, final StringBuilder source, final SignatureDefinition def) {
		source.append(MessageFormat.format("\tpublic static class {0}_template '{'\n", def.genName));
		source.append("\t\t// all the parameters\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("\t\tprivate {0} param_{1};\n", formalPar.mJavaTemplateName, formalPar.mJavaName));
		}
		if (def.returnType != null) {
			source.append(MessageFormat.format("\t\tprivate {0} reply_value;\n", def.returnType.mJavaTemplateName));
		}

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to unbound/uninitialized template.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template() '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("\t\t\tparam_{0} = new {1}(template_sel.ANY_VALUE);\n", formalPar.mJavaName, formalPar.mJavaTemplateName));
		}
		source.append("\t\t}\n\n");

		if (def.formalParameters.isEmpty()) {
			if (aData.isDebug()) {
				source.append("\t\t/**\n");
				source.append("\t\t * Initializes to an empty specific value template.\n");
				source.append("\t\t *\n");
				source.append("\t\t * @param otherValue\n");
				source.append("\t\t *                the null value.\n");
				source.append("\t\t * */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0}_template(final TitanNull_Type otherValue) '{'\n", def.genName));
			if (def.returnType != null) {
				source.append(MessageFormat.format("\t\t\treply_value = new {0}(template_sel.ANY_VALUE);\n", def.returnType.mJavaTemplateName));
			}
			source.append("\t\t}\n\n");
		}

		if (aData.isDebug()) {
			source.append("\t\t/**\n");
			source.append("\t\t * Initializes to a given template.\n");
			source.append("\t\t * The elements of the provided template are copied.\n");
			source.append("\t\t *\n");
			source.append("\t\t * @param otherValue\n");
			source.append("\t\t *                the value to initialize to.\n");
			source.append("\t\t * */\n");
		}
		source.append(MessageFormat.format("\t\tpublic {0}_template(final {0}_template otherValue) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("\t\t\tparam_{0} = new {1}(otherValue.get_field_{0}());\n", formalPar.mJavaName, formalPar.mJavaTemplateName));
		}
		source.append("\t\t}\n\n");

		if (def.formalParameters.isEmpty()) {
			if ( aData.isDebug() ) {
				source.append("\t\t/**\n");
				source.append("\t\t * Sets the current value to unbound.\n");
				source.append("\t\t * Overwriting the current content in the process.\n");
				source.append("\t\t *<p>\n");
				source.append("\t\t * operator= in the core.\n");
				source.append("\t\t *\n");
				source.append("\t\t * @param otherValue\n");
				source.append("\t\t *                the other value to assign.\n");
				source.append("\t\t * @return the new value object.\n");
				source.append("\t\t */\n");
			}
			source.append(MessageFormat.format("\t\tpublic {0}_template operator_assign(final TitanNull_Type otherValue) '{'\n", def.genName));
			source.append("\t\t\treturn this;\n");
			source.append("\t\t}\n\n");
		}

		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			source.append(MessageFormat.format("\t\tpublic {0} get_field_{1}() '{'\n", formalPar.mJavaTemplateName, formalPar.mJavaName ));
			source.append(MessageFormat.format("\t\t\treturn param_{0};\n", formalPar.mJavaName ));
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tpublic {0} constGet_field_{1}() '{'\n", formalPar.mJavaTemplateName, formalPar.mJavaName ));
			source.append(MessageFormat.format("\t\t\treturn param_{0};\n", formalPar.mJavaName ));
			source.append("\t\t}\n");
		}

		if (def.returnType != null) {
			source.append(MessageFormat.format("\t\tpublic {0} return_value() '{'\n", def.returnType.mJavaTemplateName));
			source.append("\t\t\treturn reply_value;\n");
			source.append("\t\t}\n");
		}

		source.append(MessageFormat.format("\t\tpublic {0}_call create_call() '{'\n", def.genName));
		source.append(MessageFormat.format("\t\t\t{0}_call return_value = new {0}_call();\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\treturn_value.get_field_{0}().operator_assign(param_{0}.valueof());\n", formalPar.mJavaName ));
			}
		}
		source.append("\t\t\treturn return_value;\n");
		source.append("\t\t}\n");

		if(!def.isNoBlock) {
			source.append(MessageFormat.format("\t\tpublic {0}_reply create_reply() '{'\n", def.genName));
			source.append(MessageFormat.format("\t\t\t{0}_reply return_value = new {0}_reply();\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\treturn_value.get_field_{0}().operator_assign(param_{0}.valueof());\n", formalPar.mJavaName ));
				}
			}

			if (def.returnType != null) {
				source.append("\t\t\treturn_value.get_return_value().operator_assign(reply_value.valueof());\n");
			}
			source.append("\t\t\treturn return_value;\n");
			source.append("\t\t}\n");
		}

		source.append(MessageFormat.format("\t\tpublic boolean match_call(final {0}_call match_value) '{'\n", def.genName));
		source.append("\t\t\treturn match_call(match_value, false);\n");
		source.append("\t\t}\n");

		source.append(MessageFormat.format("\t\tpublic boolean match_call(final {0}_call match_value, final boolean legacy) '{'\n", def.genName));
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tif (!param_{0}.match(match_value.get_field_{0}(), legacy)) '{'\n", formalPar.mJavaName ));
				source.append("\t\t\t\treturn false;\n");
				source.append("\t\t\t}\n");
			}
		}
		source.append("\t\t\treturn true;\n");
		source.append("\t\t}\n");

		if(!def.isNoBlock) {
			source.append(MessageFormat.format("\t\tpublic boolean match_reply(final {0}_reply match_value) '{'\n", def.genName));
			source.append("\t\t\treturn match_reply(match_value, false);\n");
			source.append("\t\t}\n");

			source.append(MessageFormat.format("\t\tpublic boolean match_reply(final {0}_reply match_value, final boolean legacy) '{'\n", def.genName));
			for (int i = 0 ; i < def.formalParameters.size(); i++) {
				final SignatureParameter formalPar = def.formalParameters.get(i);

				if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
					source.append(MessageFormat.format("\t\t\tif (!param_{0}.match(match_value.get_field_{0}(), legacy)) '{'\n", formalPar.mJavaName ));
					source.append("\t\t\t\treturn false;\n");
					source.append("\t\t\t}\n");
				}
			}
			if (def.returnType != null) {
				source.append("\t\t\tif (!reply_value.match(match_value.get_return_value(), legacy)) {\n");
				source.append("\t\t\t\treturn false;\n");
				source.append("\t\t\t}\n");
			}
			source.append("\t\t\treturn true;\n");
			source.append("\t\t}\n");
		}

		if (def.returnType != null) {
			source.append(MessageFormat.format("\t\tpublic {0}_template set_value_template(final {1} new_template) '{'\n", def.genName, def.returnType.mJavaTypeName));
			source.append(MessageFormat.format("\t\t\treturn set_value_template(new {0}(new_template));\n", def.returnType.mJavaTemplateName));
			source.append("\t\t}\n");
			source.append(MessageFormat.format("\t\tpublic {0}_template set_value_template(final {1} new_template) '{'\n", def.genName, def.returnType.mJavaTemplateName));
			source.append(MessageFormat.format("\t\t\tfinal {0}_template temp = new {0}_template(this);\n", def.genName));
			source.append("\t\t\ttemp.reply_value = new_template;\n");
			source.append("\t\t\treturn temp;\n");
			source.append("\t\t}\n");
		}

		source.append("\t\tpublic void encode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tparam_{0}.encode_text(text_buf);\n", formalPar.mJavaName ));
			}
		}
		source.append("\t\t}\n");

		source.append("\t\tpublic void decode_text(final Text_Buf text_buf) {\n");
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				source.append(MessageFormat.format("\t\t\tparam_{0}.decode_text(text_buf);\n", formalPar.mJavaName ));
			}
		}
		source.append("\t\t}\n\n");

		source.append("\t\tpublic void log() {\n");
		boolean isFirst = true;
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if (isFirst) {
				source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"'{' {0} := \");\n", formalPar.mJavaName));
				isFirst = false;
			} else {
				source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\", {0} := \");\n", formalPar.mJavaName));
			}
			source.append(MessageFormat.format("\t\t\tparam_{0}.log();\n", formalPar.mJavaName ));
		}
		source.append("\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		source.append("\t\t}\n");

		source.append(MessageFormat.format("\t\tpublic void log_match_call(final {0}_call match_value, final boolean legacy) '{'\n", def.genName));
		isFirst = true;
		for (int i = 0 ; i < def.formalParameters.size(); i++) {
			final SignatureParameter formalPar = def.formalParameters.get(i);

			if(formalPar.direction != signatureParamaterDirection.PAR_OUT) {
				if (isFirst) {
					source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"'{' {0} := \");\n", formalPar.mJavaName ));
					isFirst = false;
				} else {
					source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\", {0} := \");\n", formalPar.mJavaName ));
				}
				source.append(MessageFormat.format("\t\t\tparam_{0}.log_match(match_value.get_field_{0}(), legacy);\n", formalPar.mJavaName ));
			}
		}
		source.append("\t\t\tTTCN_Logger.log_event_str(\" }\");\n");
		source.append("\t\t}\n");

		if(!def.isNoBlock) {
			source.append(MessageFormat.format("\t\tpublic void log_match_reply(final {0}_reply match_value, final boolean legacy) '{'\n", def.genName));
			if (!def.formalParameters.isEmpty()) {
				isFirst = true;
				for (int i = 0 ; i < def.formalParameters.size(); i++) {
					final SignatureParameter formalPar = def.formalParameters.get(i);

					if(formalPar.direction != signatureParamaterDirection.PAR_IN) {
						if (isFirst) {
							source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\"'{' {0} := \");\n", formalPar.mJavaName ));
							isFirst = false;
						} else {
							source.append(MessageFormat.format("\t\t\tTTCN_Logger.log_event_str(\", {0} := \");\n", formalPar.mJavaName ));
						}
						source.append(MessageFormat.format("\t\t\tparam_{0}.log_match(match_value.get_field_{0}(), legacy);\n", formalPar.mJavaName ));
					}
				}
				if (def.returnType != null) {
					source.append("\t\t\tTTCN_Logger.log_event_str(\" } value \");\n");
					source.append("\t\t\treply_value.log_match(match_value.get_return_value(), legacy);\n");
				}
			} else {
				if (def.returnType == null) {
					source.append("\t\t\tTTCN_Logger.log_event_str(\"{ } with {} matched\");\n");
				} else {
					source.append("\t\t\tTTCN_Logger.log_event_str(\"{ } with {} matched value \");\n");
					source.append("\t\t\treply_value.log_match(match_value.get_return_value(), legacy);\n");
				}
			}
			source.append("\t\t}\n");
		}

		source.append("\t}\n");
	}
}
