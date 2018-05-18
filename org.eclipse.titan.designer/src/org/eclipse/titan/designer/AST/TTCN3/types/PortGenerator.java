package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.compiler.JavaGenData;

public class PortGenerator {

	// The kind of the testport
	public enum TestportType {NORMAL, INTERNAL, ADDRESS};

	/**
	 * Structure to describe in and out messages.
	 *
	 * originally port_msg_type is something like this
	 * */
	public static class messageTypeInfo {
		/** Java type name of the message */
		private String mJavaTypeName;

		/** Java template name of the message */
		private String mJavaTemplateName;

		/** The name to be displayed to the user */
		private String mDisplayName;

		/**
		 * @param messageType the string representing the value type of this message in the generated code.
		 * @param messageTemplate the string representing the template type of this message in the generated code.
		 * @param displayName the string representing the name to be displayed for the user.
		 * */
		public messageTypeInfo(final String messageType, final String messageTemplate, final String displayName) {
			mJavaTypeName = messageType;
			mJavaTemplateName = messageTemplate;
			mDisplayName = displayName;
		}
	}

	/**
	 * Structure to describe in and out messages.
	 *
	 * originally port_proc_signature is something like this
	 * */
	public static final class procedureSignatureInfo {
		private String mJavaTypeName;
		private String mDisplayName;
		private boolean isNoBlock;
		private boolean hasExceptions;
		private boolean hasReturnValue;

		public procedureSignatureInfo(final String procedureType, final String displayName, final boolean isNoBlock, final boolean hasExceptions, final boolean hasReturnValue) {
			this.mJavaTypeName = procedureType;
			this.mDisplayName = displayName;
			this.isNoBlock = isNoBlock;
			this.hasExceptions = hasExceptions;
			this.hasReturnValue = hasReturnValue;
		}
	}

	/**
	 * Structure describing all data needed to generate the port.
	 *
	 * originally port_def
	 * */
	public static class PortDefinition {
		/** Java type name of the port */
		public String javaName;

		/** The original name in the TTCN-3 code */
		public String displayName;

		/** The name of address in the actual module */
		public String addressName;

		/** The list of incoming messages */
		public ArrayList<messageTypeInfo> inMessages = new ArrayList<PortGenerator.messageTypeInfo>();

		/** The list of outgoing messages */
		public ArrayList<messageTypeInfo> outMessages = new ArrayList<PortGenerator.messageTypeInfo>();

		public ArrayList<procedureSignatureInfo> inProcedures = new ArrayList<PortGenerator.procedureSignatureInfo>();

		public ArrayList<procedureSignatureInfo> outProcedures = new ArrayList<PortGenerator.procedureSignatureInfo>();

		public StringBuilder varDefs;
		public StringBuilder varInit;

		/** The type of the testport */
		public TestportType testportType;

		public PortDefinition(final String genName, final String displayName) {
			javaName = genName;
			this.displayName = displayName;
		}
	}

	private PortGenerator() {
		// private to disable instantiation
	}

	/**
	 * This function can be used to generate the class of port types
	 *
	 * defPortClass in compiler2/port.{h,c}
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * */
	public static void generateClass(final JavaGenData aData, final StringBuilder source, final PortDefinition portDefinition) {
		aData.addImport("java.util.LinkedList");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport("Index_Redirect");
		aData.addBuiltinTypeImport( "TitanPort" );
		aData.addBuiltinTypeImport( "TitanAlt_Status" );
		aData.addBuiltinTypeImport( "TitanComponent");
		aData.addBuiltinTypeImport( "TitanOctetString");
		aData.addBuiltinTypeImport( "Base_Type" );
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );
		aData.addBuiltinTypeImport("TtcnLogger");
		aData.addBuiltinTypeImport("TitanLoggerApi");
		aData.addCommonLibraryImport("Text_Buf");
		aData.addBuiltinTypeImport("TtcnError");
		if (portDefinition.testportType != TestportType.INTERNAL) {
			aData.addImport("org.eclipse.titan.user_provided." + portDefinition.javaName);
		}

		boolean hasIncomingReply = false;
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			if (!info.isNoBlock) {
				hasIncomingReply = true;
			}
		}
		boolean hasIncomingException = false;
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			if (info.hasExceptions) {
				hasIncomingException = true;
			}
		}


		generateDeclaration(source, portDefinition);

		for (int i = 0 ; i < portDefinition.outMessages.size(); i++) {
			final messageTypeInfo outType = portDefinition.outMessages.get(i);

			generateSend(source, outType, portDefinition);
		}

		if (portDefinition.inMessages.size() > 0) {
			aData.addBuiltinTypeImport("TitanCharString");
			aData.addBuiltinTypeImport("TitanComponent_template");

			generateGenericReceive(source, portDefinition, false, false);
			generateGenericReceive(source, portDefinition, true, false);
			generateGenericTrigger(source, portDefinition, false);

			if (portDefinition.testportType == TestportType.ADDRESS) {
				generateGenericReceive(source, portDefinition, false, true);
				generateGenericReceive(source, portDefinition, true, true);
				generateGenericTrigger(source, portDefinition, true);
			}

			generateProcessMessage(source, portDefinition);
		}

		// generic and simplified receive for experimentation
		for (int i = 0 ; i < portDefinition.inMessages.size(); i++) {
			final messageTypeInfo inType = portDefinition.inMessages.get(i);

			generateTypedReceive(source, i, inType, false);
			generateTypedReceive(source, i, inType, true);
			generateTypeTrigger(source, i, inType);
			generateTypedIncomminMessage(source, i, inType, portDefinition);
		}

		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			generateCallFunction(source, info, portDefinition);
		}
		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

			generateReplyFunction(source, info, portDefinition);
		}
		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

			generateRaiseFunction(source, info, portDefinition);
		}

		//FIXME more complicated conditional
		if (portDefinition.testportType != TestportType.INTERNAL) {
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

				source.append(MessageFormat.format("public abstract void outgoing_call(final {0}_call call_par", info.mJavaTypeName));
				if (portDefinition.testportType == TestportType.ADDRESS) {
					source.append(MessageFormat.format(", final {0} destination_address", portDefinition.addressName));
				}
				source.append(");\n");
			}
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

				if (!info.isNoBlock) {
					source.append(MessageFormat.format("public abstract void outgoing_reply(final {0}_reply reply_par", info.mJavaTypeName));
					if (portDefinition.testportType == TestportType.ADDRESS) {
						source.append(MessageFormat.format(", final {0} destination_address", portDefinition.addressName));
					}
					source.append(");\n");
				}
			}
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

				if (info.hasExceptions) {
					source.append(MessageFormat.format("public abstract void outgoing_raise(final {0}_exception raise_exception", info.mJavaTypeName));
					if (portDefinition.testportType == TestportType.ADDRESS) {
						source.append(MessageFormat.format(", final {0} destination_address", portDefinition.addressName));
					}
					source.append(");\n");
				}
			}
		}

		if (portDefinition.inProcedures.size() > 0) {
			generateGenericGetcall(source, portDefinition, false, false);
			generateGenericGetcall(source, portDefinition, true, false);
			if (portDefinition.testportType == TestportType.ADDRESS) {
				generateGenericGetcall(source, portDefinition, false, true);
				generateGenericGetcall(source, portDefinition, true, true);
			}

			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

				generateTypedGetcall(source, portDefinition, i, info, false, false);
				generateTypedGetcall(source, portDefinition, i, info, true, false);
				if (portDefinition.testportType == TestportType.ADDRESS) {
					generateTypedGetcall(source, portDefinition, i, info, false, true);
					generateTypedGetcall(source, portDefinition, i, info, true, true);
				}
			}
		}

		if (hasIncomingReply) {
			generateGenericGetreply(source, portDefinition, false, false);
			generateGenericGetreply(source, portDefinition, true, false);
			if (portDefinition.testportType == TestportType.ADDRESS) {
				generateGenericGetreply(source, portDefinition, false, true);
				generateGenericGetreply(source, portDefinition, true, true);
			}

			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

				if (!portDefinition.outProcedures.get(i).isNoBlock) {
					generateTypedGetreply(source, portDefinition, i, info, false, false);
					generateTypedGetreply(source, portDefinition, i, info, true, false);
					if (portDefinition.testportType == TestportType.ADDRESS) {
						generateTypedGetreply(source, portDefinition, i, info, false, true);
						generateTypedGetreply(source, portDefinition, i, info, true, true);
					}
				}
			}
		}

		if (hasIncomingException) {
			generateGenericGetexception(source, portDefinition, false, false);
			generateGenericGetexception(source, portDefinition, true, false);
			if (portDefinition.testportType == TestportType.ADDRESS) {
				generateGenericGetexception(source, portDefinition, false, true);
				generateGenericGetexception(source, portDefinition, true, true);
			}

			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

				if (portDefinition.outProcedures.get(i).hasExceptions) {
					generateTypedGetexception(source, portDefinition, i, info, false, false);
					generateTypedGetexception(source, portDefinition, i, info, true, false);
					if (portDefinition.testportType == TestportType.ADDRESS) {
						generateTypedGetexception(source, portDefinition, i, info, false, true);
						generateTypedGetexception(source, portDefinition, i, info, true, true);
					}
				}
			}
		}

		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

			generateTypedIcomingCall(source, i, info, portDefinition);
		}

		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			if (!info.isNoBlock) {
				generateTypedIcomingReply(source, i, info, portDefinition);
			}
		}

		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			if (info.hasExceptions) {
				generateTypedIcomingException(source, i, info, portDefinition);
			}
		}

		if (portDefinition.inProcedures.size() > 0) {
			generateProcessCall(source, portDefinition);
		}
		if (hasIncomingReply) {
			generateProcessReply(source, portDefinition);
		}
		if (hasIncomingException) {
			generateProcessException(source, portDefinition);
		}

		source.append( "//TODO: port code generation is not yet fully implemented!\n" );

		source.append("}\n\n");
	}

	/**
	 * This function generates the declaration of the generated port type class.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateDeclaration(final StringBuilder source, final PortDefinition portDefinition) {
		String className;
		String baseClassName;
		String abstractNess;
		if (portDefinition.testportType == TestportType.INTERNAL) {
			abstractNess = "";
			className = portDefinition.javaName;
			baseClassName = "TitanPort";
		} else {
			// FIXME more complicated
			abstractNess = "abstract";
			className = portDefinition.javaName + "_BASE";
			baseClassName = "TitanPort";
		}
		source.append(MessageFormat.format("public static {0} class {1} extends {2} '{'\n", abstractNess, className, baseClassName));

		if(portDefinition.inMessages.size() > 0) {
			source.append("enum message_selection { ");
			for (int i = 0 ; i < portDefinition.inMessages.size(); i++) {
				if (i > 0) {
					source.append(", ");
				}
				source.append(MessageFormat.format("MESSAGE_{0}", i));
			}
			source.append("};\n");

			source.append("private class Message_queue_item {\n");
			source.append("message_selection item_selection;\n");
			source.append("// base type could be: ");
			for (int i = 0 ; i < portDefinition.inMessages.size(); i++) {
				final messageTypeInfo inType = portDefinition.inMessages.get(i);

				if (i > 0) {
					source.append(", ");
				}
				source.append(inType.mJavaTypeName);
			}
			source.append('\n');
			source.append("Base_Type message;\n");
			source.append("int sender_component;\n");
			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(MessageFormat.format("{0} sender_address;\n", portDefinition.addressName));
			}
			source.append("}\n");

			source.append("private LinkedList<Message_queue_item> message_queue = new LinkedList<Message_queue_item>();\n\n");

			source.append("private void remove_msg_queue_head() {\n");
			source.append("message_queue.removeFirst();\n");
			source.append("}\n\n");

			source.append("protected void clear_queue() {\n");
			source.append("message_queue.clear();\n");
			source.append("}\n\n");
		}

		final boolean hasIncomingCall = portDefinition.inProcedures.size() > 0;
		boolean hasIncomingReply = false;
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			if (!portDefinition.outProcedures.get(i).isNoBlock) {
				hasIncomingReply = true;
			}
		}
		boolean hasIncomingException = false;
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			if (portDefinition.outProcedures.get(i).hasExceptions) {
				hasIncomingException = true;
			}
		}

		final boolean hasProcedureQueue = hasIncomingCall || hasIncomingReply || hasIncomingException;
		if (hasProcedureQueue) {
			source.append("enum proc_selection { ");
			boolean isFirst = true;
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				if (!isFirst) {
					source.append(", ");
				}
				isFirst = false;
				source.append(MessageFormat.format("CALL_{0}", i));
			}
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				if (!portDefinition.outProcedures.get(i).isNoBlock) {
					if (!isFirst) {
						source.append(", ");
					}
					isFirst = false;
					source.append(MessageFormat.format("REPLY_{0}", i));
				}
			}
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				if (portDefinition.outProcedures.get(i).hasExceptions) {
					if (!isFirst) {
						source.append(", ");
					}
					isFirst = false;
					source.append(MessageFormat.format("EXCEPTION_{0}", i));
				}
			}
			source.append("};\n");

			source.append("private class Procedure_queue_item {\n");
			source.append("proc_selection item_selection;\n");
			source.append("// TODO check if an object would be enough \n");
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				source.append(MessageFormat.format("{0}_call call_{1};\n", portDefinition.inProcedures.get(i).mJavaTypeName, i));
			}
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.outProcedures.get(i);
				if (!info.isNoBlock) {
					source.append(MessageFormat.format("{0}_reply reply_{1};\n", info.mJavaTypeName, i));
				}
			}
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				final procedureSignatureInfo info = portDefinition.outProcedures.get(i);
				if (info.hasExceptions) {
					source.append(MessageFormat.format("{0}_exception exception_{1};\n", info.mJavaTypeName, i));
				}
			}
			source.append("int sender_component;\n");
			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(MessageFormat.format("{0} sender_address;\n", portDefinition.addressName));
			}
			source.append("}\n");
			source.append("private LinkedList<Procedure_queue_item> procedure_queue = new LinkedList<Procedure_queue_item>();\n");
			source.append("private void remove_proc_queue_head() {\n");
			source.append("procedure_queue.removeFirst();\n");
			source.append("TtcnLogger.log_port_queue(TitanLoggerApi.Port__Queue_operation.enum_type.extract__op, get_name(), 0 , ++proc_head_count, new TitanCharString(\"\"), new TitanCharString(\"\"));");
			source.append("}\n\n");
		}

		if (portDefinition.varDefs != null) {
			source.append(portDefinition.varDefs);
		}
		if (portDefinition.varInit != null) {
			source.append("\n");
			source.append("private void init_port_variables() {\n");
			source.append(portDefinition.varInit);
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("public {0}( final String port_name) '{'\n", className));
		source.append("super(port_name);\n");
		source.append("}\n\n");

		//FIXME more complicated conditional
		if (portDefinition.testportType == TestportType.INTERNAL) {
			source.append(MessageFormat.format("public {0}( ) '{'\n", className));
			source.append(MessageFormat.format("this((String)null);\n", className));
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the sending functions.
	 *
	 * @param source where the source code is to be generated.
	 * @param outType the information about the outgoing message.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateSend(final StringBuilder source, final messageTypeInfo outType, final PortDefinition portDefinition) {
		source.append(MessageFormat.format("public void send(final {0} send_par, final TitanComponent destination_component) '{'\n", outType.mJavaTypeName));
		source.append("if (!is_started) {\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Sending a message on port {0}, which is not started.\", get_name()));\n");
		source.append("}\n");
		source.append("if (!destination_component.isBound()) {\n");
		source.append("throw new TtcnError(\"Unbound component reference in the to clause of send operation.\");\n");
		source.append("}\n");
		source.append("final TtcnLogger.Severity log_severity = destination_component.getComponent() == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_MMSEND : TtcnLogger.Severity.PORTEVENT_MCSEND;\n");
		source.append("if (TtcnLogger.log_this_event(log_severity)) {\n");
		source.append("TtcnLogger.begin_event(log_severity);\n");
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\" {0} : \");\n", outType.mDisplayName));
		source.append("send_par.log();\n");
		source.append("TtcnLogger.log_msgport_send(get_name(), destination_component.getComponent(), TtcnLogger.end_event_log2str());\n");
		source.append("}\n");
		source.append("if (destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF)) {\n");
		if (portDefinition.testportType == TestportType.INTERNAL) {
			source.append("throw new TtcnError(MessageFormat.format(\"Message cannot be sent to system on internal port {0}.\", get_name()));\n");
		} else {
			source.append("get_default_destination();\n");
			source.append("outgoing_send(send_par");
			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(", null");
			}
			source.append(");\n");
		}
		source.append("} else {\n");
		source.append("Text_Buf text_buf = new Text_Buf();\n");
		source.append(MessageFormat.format("prepare_message(text_buf, \"{0}\");\n",outType.mDisplayName));
		source.append("send_par.encode_text(text_buf);\n");
		source.append("send_data(text_buf, destination_component);\n");
		source.append("}\n");
		source.append("}\n\n");

		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format("public void send(final {0} send_par, final {1} destination_address) '{'\n", outType.mJavaTypeName, portDefinition.addressName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Sending a message on port {0}, which is not started.\", get_name()));\n");
			source.append("}\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_DUALSEND)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_DUALSEND);\n");
			source.append("send_par.log;\n");
			source.append(MessageFormat.format("TtcnLogger.log_dualport_map(0,\"{0}\", TtcnLogger.end_event_log2str(), 0);\n ",outType.mDisplayName));
			source.append("get_default_destination();\n");
			source.append("outgoing_send(send_par, destination_address);\n");
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("public void send(final {0} send_par) '{'\n", outType.mJavaTypeName));
		source.append("send(send_par, new TitanComponent(get_default_destination()));\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public void send(final {0} send_par, final TitanComponent destination_component) '{'\n", outType.mJavaTemplateName));
		source.append(MessageFormat.format("final {0} send_par_value = send_par.valueOf();\n", outType.mJavaTypeName));
		source.append("send(send_par_value, destination_component);\n");
		source.append("}\n\n");

		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format("public void send(final {0} send_par, final {1} destination_address) '{'\n", outType.mJavaTemplateName, portDefinition.addressName));
			source.append(MessageFormat.format("final {0} send_par_value = send_par.valueOf();\n", outType.mJavaTypeName));
			source.append("send(send_par_value, destination_address);\n");
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("public void send(final {0} send_par) '{'\n", outType.mJavaTemplateName));
		source.append(MessageFormat.format("final {0} send_par_value = send_par.valueOf();\n", outType.mJavaTypeName));
		source.append("send(send_par_value, new TitanComponent(get_default_destination()));\n");
		source.append("}\n\n");

		// FIXME a bit more complex expression
		if (portDefinition.testportType != TestportType.INTERNAL) {
			source.append(MessageFormat.format("public abstract void outgoing_send(final {0} send_par", outType.mJavaTypeName));
			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(MessageFormat.format(", {0} destination_address", portDefinition.addressName));
			}
			source.append(");\n\n");
		}
	}

	/**
	 * This function generates the generic receive or check(receive) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param portDefinition the definition of the port.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateGenericReceive(final StringBuilder source, final PortDefinition portDefinition, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_receive" : "receive";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";
		final String logger_operation = isCheck ? "check__receive__op" : "receive__op";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template sender_template, final {1} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, senderType));
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");

		source.append("Message_queue_item my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append(" } else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");

		if (isAddress) {
			source.append("if (my_head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_MMUNSUCC, MessageFormat.format(\"Matching on port {0} failed: Sender of the first message in the queue is not the system.\" ,get_name()));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (my_head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name());\n", functionName));
			source.append("} else if (!sender_template.match(my_head.sender_address, false)) {\n");
			source.append("if(TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_MMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_MMUNSUCC);");
			source.append("TtcnLogger.log_event(\"Matching on port {0}: Sender address of the first message in the queue does not match the from clause: \", get_name());\n");
			source.append("sender_template.log_match(new TitanComponent(my_head.sender_adress), false);\n");
			source.append("TtcnLogger.end_event()");
			source.append("//FIXME: TTCN_Logger::log_matching_failure() missing\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		} else {
			source.append("if (!sender_template.match(my_head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC:TtcnLogger.Severity.MATCHING_MCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event(\"Matching on port {0}: Sender of the first message in the queue does not match the from clause:\", get_name());\n");
			source.append("sender_template.log_match(new TitanComponent(my_head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		}

		source.append(" else {\n");
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(my_head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(my_head.sender_component);\n");
		}
		source.append("}\n");
		if(isAddress) {
			source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_MMSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_MMRECV)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_msgport_recv(get_name(), TitanLoggerApi.Msg__port__recv_operation.enum_type.{0} , TitanComponent.SYSTEM_COMPREF, new TitanCharString(\"\") ,", logger_operation));
			source.append("(TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_MMRECV), my_head.sender_adress.log(), TtcnLogger.end_event_log2str()), ");
			source.append("msg_head_count+1);\n");
			source.append("}\n");
		} else {
			source.append("TtcnLogger.log(my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMSUCCESS : TtcnLogger.Severity.MATCHING_MCSUCCESS, ");
			source.append(" MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("final TtcnLogger.Severity log_sev = my_head.sender_component==TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_MMRECV : TtcnLogger.Severity.PORTEVENT_MCRECV;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("switch (my_head.item_selection) {\n");
			for (int msg_idx = 0; msg_idx < portDefinition.inMessages.size(); msg_idx++) {
				final messageTypeInfo message_type = portDefinition.inMessages.get(msg_idx);
				source.append(MessageFormat.format("case MESSAGE_{0}:\n", msg_idx));
				source.append("TtcnLogger.begin_event(log_sev);\n");
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\": {0}: \");\n", message_type.mDisplayName));
				source.append("my_head.message.log();\n");
				source.append(MessageFormat.format("TtcnLogger.log_msgport_recv(get_name(), TitanLoggerApi.Msg__port__recv_operation.enum_type.{0}, ", logger_operation));
				source.append("my_head.sender_component, new TitanCharString(\"\"),");
				source.append(MessageFormat.format("TtcnLogger.end_event_log2str(), msg_head_count+1);\n", msg_idx));
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append("throw new TtcnError(\"Internal error: unknown message\");\n");
			source.append("}\n");
			source.append("}\n");
		}
		if (!isCheck) {
			source.append("remove_msg_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the generic trigger function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param portDefinition the definition of the port.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateGenericTrigger(final StringBuilder source, final PortDefinition portDefinition, final boolean isAddress) {
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status trigger(final {0}_template sender_template, final {0} sender_pointer, final Index_Redirect index_redirect) '{'\n", senderType));
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");

		source.append("Message_queue_item my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append(" } else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");

		if (isAddress) {
			source.append("if (my_head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_MMUNSUCC, \"Matching on port {0} will drop a message: Sender of the first message in the queue is not the system.\", get_name());\n ");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (my_head.sender_address == null) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Trigger operation on port {0} requires the address of the sender, which was not given by the test port.\", get_name()));\n");
			source.append("} else if (!sender_template.match(my_head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_MMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_MMUNSUCC);\n");
			source.append("TtcnLogger.log_event(\"Matching on port {0}: Sender address of the first message in the queue does not match the from clause: \", port_name);\n");
			source.append("sender_template.log_match(new TitanComponent(my_head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("//FIXME: TTCN_Logger::log_matching_failure() missing\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_REPEAT;\n");
			source.append("}\n");
			source.append("}\n");
		} else {
			source.append("if (!sender_template.match(my_head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC:TtcnLogger.Severity.MATCHING_MCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event(\"Matching on port {0}  will drop a message: Sender of the first message in the queue does not match the from clause: \" , get_name());\n");
			source.append("sender_template.log_match( new TitanComponent(my_head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_REPEAT;\n");
			source.append('}');
		}

		source.append(" else {\n");
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(my_head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(my_head.sender_component);\n");
		}
		source.append("}\n");
		if(isAddress) {
			source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_MMSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_MMRECV)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_MMRECV);\n");
			source.append("my_head.sender_address.log();\n");
			source.append("TtcnLogger.log_msgport_recv(get_name(), TitanLoggerApi.Msg__port__recv_operation.enum_type.trigger__op, TitanComponent.SYSTEM_COMPREF, new TitanCharString(\"\")), TtcnLogger.end_event_log2str(), msg_head_count+1);\n");
		} else {
			source.append("TtcnLogger.log(my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMSUCCESS : TtcnLogger.Severity.MATCHING_MCSUCCESS, ");
			source.append(" MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("final TtcnLogger.Severity log_sev = my_head.sender_component==TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_MMRECV : TtcnLogger.Severity.PORTEVENT_MCRECV;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("switch (my_head.item_selection) {\n");
			for (int msg_idx = 0; msg_idx < portDefinition.inMessages.size(); msg_idx++) {
				final messageTypeInfo message_type = portDefinition.inMessages.get(msg_idx);
				source.append(MessageFormat.format("case MESSAGE_{0}:\n", msg_idx));
				source.append("TtcnLogger.begin_event(log_sev);\n");
				source.append(MessageFormat.format("TtcnLogger.log_event_str(\": {0}: \");\n", message_type.mDisplayName));
				source.append("my_head.message.log();\n");
				source.append("TtcnLogger.log_msgport_recv(get_name(), TitanLoggerApi.Msg__port__recv_operation.enum_type.trigger__op, ");
				source.append("my_head.sender_component, new TitanCharString(\"\"),");
				source.append(MessageFormat.format("TtcnLogger.end_event_log2str(), msg_head_count+1);\n", msg_idx));
				source.append("break;\n");
			}
			source.append("default:\n");
			source.append("throw new TtcnError(\"Internal error: unknown message\");\n");
			source.append("}\n");
			source.append("}\n");
		}
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the receive or check(receive) function for a type
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this message type has in the declaration the port type.
	 * @param inType the information about the incoming message.
	 * @param isCheck generate the check or the non-checking version.
	 * */
	private static void generateTypedReceive(final StringBuilder source, final int index, final messageTypeInfo inType, final boolean isCheck) {
		final String typeValueName = inType.mJavaTypeName;
		final String typeTemplateName = inType.mJavaTemplateName;
		final String functionName = isCheck ? "check_receive" : "receive";
		final String operationName = isCheck ? "check__receive__op" : "receive__op";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1} value_template, final {2} value_redirect, final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, typeTemplateName, typeValueName));
		source.append("if (value_template.get_selection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("throw new TtcnError(\"Receive operation using '*' as matching template\");\n");
		source.append("}\n");
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n\n");
		source.append("Message_queue_item my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("} else if (!sender_template.match(my_head.sender_component, false)) {\n");
		source.append("final TtcnLogger.Severity log_sev = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC : TtcnLogger.Severity.MATCHING_MCUNSUCC;\n");
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(log_sev);\n");
		source.append("TtcnLogger.log_event_str(MessageFormat.format(\"Matching on port {0} failed: Sender of the first message in the queue does not match the from clause: \", get_name()));\n");
		source.append("sender_template.log_match(new TitanComponent(my_head.sender_component), false);\n");
		source.append("TtcnLogger.end_event();\n");
		source.append("}\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0} || !(my_head.message instanceof {1})) '{'\n", index, typeValueName));
		source.append(MessageFormat.format("TtcnLogger.log_str(my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC : TtcnLogger.Severity.MATCHING_MCUNSUCC, MessageFormat.format(\"Matching on port '{'0'}' failed: Type of the first message in the queue is not {0}.\", get_name()));\n", typeValueName));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!value_template.match(({0}) my_head.message)) '{'\n", typeValueName));
		source.append("final TtcnLogger.Severity log_sev = TtcnLogger.Severity.MATCHING_MMUNSUCC;\n");
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(log_sev);\n");
		source.append("value_template.log_match(my_head.message, false);\n");
		source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.message__, get_name(), my_head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.message__does__not__match__template, TtcnLogger.end_event_log2str());\n");
		source.append("}\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(" } else {\n");
		source.append("if (value_redirect != null) {\n");
		source.append(MessageFormat.format("value_redirect.assign(({0}) my_head.message);\n", typeValueName));
		source.append("}\n");
		source.append("if (sender_pointer != null) {\n");
		source.append("sender_pointer.assign(my_head.sender_component);\n");
		source.append("}\n");
		source.append("TtcnLogger.Severity log_severity = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMSUCCESS : TtcnLogger.Severity.MATCHING_MCSUCCESS;\n");
		source.append("if (TtcnLogger.log_this_event(log_severity)) {\n");
		source.append("TtcnLogger.begin_event_log2str();\n");
		source.append("value_template.log_match(my_head.message, true);\n");
		source.append("TitanCharString temp = TtcnLogger.end_event_log2str();\n");
		source.append("TtcnLogger.log_matching_success(TitanLoggerApi.PortType.enum_type.message__, port_name, my_head.sender_component, temp);\n");
		source.append("}\n");
		source.append("log_severity = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_MMRECV : TtcnLogger.Severity.PORTEVENT_MCRECV;\n");
		source.append("if (TtcnLogger.log_this_event(log_severity)) {\n");
		source.append("TtcnLogger.begin_event_log2str();\n");
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\": {0} : \");\n", inType.mDisplayName));
		source.append("my_head.message.log();\n");
		source.append("TitanCharString temp = TtcnLogger.end_event_log2str();\n");
		source.append(MessageFormat.format("TtcnLogger.log_msgport_recv(port_name, TitanLoggerApi.Msg__port__recv_operation.enum_type.{0}, my_head.sender_component, new TitanCharString(\"\"), temp, msg_head_count + 1);\n", operationName));
		source.append("}\n");
		if (!isCheck) {
			source.append("remove_msg_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the trigger function for a type
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this message type has in the declaration the port type.
	 * @param inType the information about the incoming message.
	 * */
	private static void generateTypeTrigger(final StringBuilder source, final int index, final messageTypeInfo inType) {
		final String typeValueName = inType.mJavaTypeName;
		final String typeTemplateName = inType.mJavaTemplateName;

		source.append(MessageFormat.format("public TitanAlt_Status trigger(final {0} value_template, final {1} value_redirect, final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) '{'\n", typeTemplateName, typeValueName));
		source.append("if (value_template.get_selection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("throw new TtcnError(\"Trigger operation using '*' as matching template\");\n");
		source.append("}\n");
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_PROBLEM, \"Matching on port {0} will drop a message: Port is not started and the queue is empty.\", get_name());\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n\n");
		source.append("Message_queue_item my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_PROBLEM, \"Matching on port {0} will drop a message: Port is not started and the queue is empty.\", get_name());\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("} else if (!sender_template.match(my_head.sender_component, false)) {\n");
		source.append("final TtcnLogger.Severity log_sev = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC : TtcnLogger.Severity.MATCHING_MCUNSUCC;\n");
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(log_sev);\n");
		source.append("TtcnLogger.log_event(\"Matching on port {0} will drop a message: Sender of the first message in the queue does not match the from clause: \", get_name());\n");
		source.append("sender_template.log_match(new TitanComponent(my_head.sender_component), false);\n");
		source.append("TtcnLogger.end_event();\n");
		source.append("}\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0} || !(my_head.message instanceof {1})) '{'\n", index, typeValueName));
		source.append("TtcnLogger.log(my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC : TtcnLogger.Severity.MATCHING_MCUNSUCC, \"Matching on port {0} will drop a message: ");
		source.append(MessageFormat.format("Type of the first message in the queue is not {0}.\", get_name());\n", typeValueName) );
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(MessageFormat.format("'}' else if (!value_template.match(({0}) my_head.message)) '{'\n", typeValueName));
		source.append("final TtcnLogger.Severity log_sev = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMUNSUCC : TtcnLogger.Severity.MATCHING_MCUNSUCC;\n");
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(log_sev);\n");
		source.append(MessageFormat.format("value_template.log_match(my_head.message, false);\n", index));
		source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.message__, get_name(), my_head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.message__does__not__match__template, TtcnLogger.end_event_log2str());\n");
		source.append("}\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(" } else {\n");
		source.append("if (value_redirect != null) {\n");
		source.append(MessageFormat.format("value_redirect.assign(({0}) my_head.message);\n", typeValueName));
		source.append("}\n");
		source.append("if (sender_pointer != null) {\n");
		source.append("sender_pointer.assign(my_head.sender_component);\n");
		source.append("}\n");
		source.append("TtcnLogger.Severity log_severity = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_MMSUCCESS : TtcnLogger.Severity.MATCHING_MCSUCCESS;\n");
		source.append("if (TtcnLogger.log_this_event(log_severity)) {\n");
		source.append("TtcnLogger.begin_event_log2str();\n");
		source.append("value_template.log_match(my_head.message, true);\n");
		source.append("TitanCharString temp = TtcnLogger.end_event_log2str();\n");
		source.append("TtcnLogger.log_matching_success(TitanLoggerApi.PortType.enum_type.message__, port_name, my_head.sender_component, temp);\n");
		source.append("}\n");
		source.append("log_severity = my_head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_MMRECV : TtcnLogger.Severity.PORTEVENT_MCRECV;\n");
		source.append("if (TtcnLogger.log_this_event(log_severity)) {\n");
		source.append("TtcnLogger.begin_event_log2str();\n");
		source.append(MessageFormat.format("TtcnLogger.log_event_str(\": {0} : \");\n", inType.mDisplayName));
		source.append("my_head.message.log();\n");
		source.append("TitanCharString temp = TtcnLogger.end_event_log2str();\n");
		source.append("TtcnLogger.log_msgport_recv(port_name, TitanLoggerApi.Msg__port__recv_operation.enum_type.trigger__op, my_head.sender_component, new TitanCharString(\"\"), temp, msg_head_count + 1);\n");
		source.append("}\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the incoming_message function for a type
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this message type has in the declaration the port type.
	 * @param inType the information about the incoming message.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateTypedIncomminMessage(final StringBuilder source, final int index, final messageTypeInfo inType, final PortDefinition portDefinition) {
		final String typeValueName = inType.mJavaTypeName;

		source.append(MessageFormat.format("private void incoming_message(final {0} incoming_par, final int sender_component", typeValueName));
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format(", final {0} sender_address", portDefinition.addressName));
		}
		source.append(") {\n");
		source.append("if (!is_started) {\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Port {0} is not started but a message has arrived on it.\", get_name()));\n");
		source.append("}\n");
		source.append("Message_queue_item new_item = new Message_queue_item();\n");
		source.append(MessageFormat.format("new_item.item_selection = message_selection.MESSAGE_{0};\n", index));
		source.append(MessageFormat.format("new_item.message = new {0}(incoming_par);\n", typeValueName));
		source.append("new_item.sender_component = sender_component;\n");
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append("if (sender_address != null) {\n");
			source.append(MessageFormat.format("new_item.sender_address = new {0}(sender_address);\n", portDefinition.addressName));
			source.append("}\n");
		}
		source.append("message_queue.addLast(new_item);\n");
		source.append("}\n\n");

		if (portDefinition.testportType != TestportType.INTERNAL) {
			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(MessageFormat.format("protected void incoming_message(final TitanInteger incoming_par, final {0} sender_address) '{'\n", portDefinition.addressName));
				source.append("incoming_message(incoming_par, TitanComponent.SYSTEM_COMPREF, sender_address);\n");
				source.append("}\n");

				source.append(MessageFormat.format("protected void incoming_message(final {0} incoming_par) '{'\n", typeValueName));
				source.append("incoming_message(incoming_par, TitanComponent.SYSTEM_COMPREF, null);\n");
				source.append("}\n\n");
			} else {
				source.append(MessageFormat.format("protected void incoming_message(final {0} incoming_par) '{'\n", typeValueName));
				source.append("incoming_message(incoming_par, TitanComponent.SYSTEM_COMPREF);\n");
				source.append("}\n\n");
			}
		}
	}

	/**
	 * This function generates the process_message function for a type
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateProcessMessage(final StringBuilder source, final PortDefinition portDefinition) {
		source.append("protected boolean process_message(final String message_type, final Text_Buf incoming_buf, final int sender_component, final TitanOctetString slider) {\n");
		for (int i = 0 ; i < portDefinition.inMessages.size(); i++) {
			final messageTypeInfo inType = portDefinition.inMessages.get(i);

			source.append(MessageFormat.format("if (\"{0}\".equals(message_type)) '{'\n", inType.mDisplayName));
			source.append(MessageFormat.format("{0} incoming_par = new {0}();\n", inType.mJavaTypeName));
			source.append("incoming_par.decode_text(incoming_buf);\n");
			source.append("incoming_message(incoming_par, sender_component);\n");
			source.append("return true;\n");
			source.append("} else ");
		}

		source.append("return false;\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the call function for a signature
	 *
	 * @param source where the source code is to be generated.
	 * @param info information about the signature type.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateCallFunction(final StringBuilder source, final procedureSignatureInfo info, final PortDefinition portDefinition) {
		source.append(MessageFormat.format("public void call(final {0}_template call_template, final TitanComponent destination_component) '{'\n", info.mJavaTypeName));
		source.append("if (!is_started) {\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Calling a signature on port {0}, which is not started.\", get_name()));\n");
		source.append("}\n");
		source.append("if (!destination_component.isBound()) {\n");
		source.append("throw new TtcnError(\"Unbound component reference in the to clause of call operation.\");\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("final {0}_call call_temp = call_template.create_call();\n", info.mJavaTypeName));
		source.append("final TtcnLogger.Severity log_sev = destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF) ? TtcnLogger.Severity.PORTEVENT_PMOUT : TtcnLogger.Severity.PORTEVENT_PCOUT;\n");
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
		source.append("call_temp.log();\n");
		source.append("TtcnLogger.log_procport_send(get_name(), TitanLoggerApi.Port__oper.enum_type.call__op, destination_component.getComponent(), new TitanCharString(\"\"), TtcnLogger.end_event_log2str());\n");
		source.append("}\n");
		source.append("if (destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF)) {\n");
		if (portDefinition.testportType == TestportType.INTERNAL) {
			source.append("throw new TtcnError(MessageFormat.format(\"Internal port {0} cannot send call to system.\", get_name()));\n");
		} else if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append("outgoing_call(call_temp, null);\n");
		} else {
			source.append("outgoing_call(call_temp);\n");
		}

		source.append("} else {\n");
		source.append("Text_Buf text_buf = new Text_Buf();\n");
		source.append(MessageFormat.format("prepare_call(text_buf, \"{0}\");\n", info.mDisplayName));
		source.append("call_temp.encode_text(text_buf);\n");
		source.append("send_data(text_buf, destination_component);\n");
		source.append("}\n");
		source.append("}\n\n");

		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format("public void call(final {0}_template call_template, final {1} destination_address) '{'\n", info.mJavaTypeName, portDefinition.addressName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Calling a signature on port {0}, which is not started.\", get_name()));\n");
			source.append("}\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMOUT)) {");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
			source.append("destination_address.log();\n");
			source.append("TitanCharString log_temp = TtcnLogger.end_event_log2str();\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
			source.append("call_template.log();\n");
			source.append("TtcnLogger.log_procport_send(get_name(), TitanLoggerApi.Port__oper.enum_type.call__op, TitanComponent.SYSTEM_COMPREF, log_temp, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append(MessageFormat.format("final {0}_call call_temp = call_template.create_call();\n", info.mJavaTypeName));
			source.append("outgoing_call(call_temp, destination_address);\n");
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("public void call(final {0}_template call_template) '{'\n", info.mJavaTypeName));
		source.append("call(call_template, new TitanComponent(get_default_destination()));\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the reply function for a signature
	 *
	 * @param source where the source code is to be generated.
	 * @param info information about the signature type.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateReplyFunction(final StringBuilder source, final procedureSignatureInfo info, final PortDefinition portDefinition) {
		if (!info.isNoBlock) {
			source.append(MessageFormat.format("public void reply(final {0}_template reply_template, final TitanComponent destination_component) '{'\n", info.mJavaTypeName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Replying to a signature on port {0}, which is not started.\", get_name()));\n");
			source.append("}\n");
			source.append("if (!destination_component.isBound()) {\n");
			source.append("throw new TtcnError(\"Unbound component reference in the to clause of reply operation.\");\n");
			source.append("}\n\n");

			source.append(MessageFormat.format("final {0}_reply reply_temp = reply_template.create_reply();\n", info.mJavaTypeName));
			source.append("final TtcnLogger.Severity log_sev = destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF) ? TtcnLogger.Severity.PORTEVENT_PMOUT : TtcnLogger.Severity.PORTEVENT_PCOUT;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
			source.append("reply_temp.log();\n");
			source.append("TtcnLogger.log_procport_send(get_name(), TitanLoggerApi.Port__oper.enum_type.reply__op, destination_component.getComponent(), new TitanCharString(\"\"), TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("if (destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF)) {\n");
			if (portDefinition.testportType == TestportType.INTERNAL) {
				source.append("throw new TtcnError(MessageFormat.format(\"Internal port {0} cannot reply to system.\", get_name()));\n");
			} else if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append("outgoing_reply(reply_temp, null);\n");
			} else {
				source.append("outgoing_reply(reply_temp);\n");
			}
			source.append("} else {\n");
			source.append("Text_Buf text_buf = new Text_Buf();\n");
			source.append(MessageFormat.format("prepare_reply(text_buf, \"{0}\");\n", info.mDisplayName));
			source.append("reply_temp.encode_text(text_buf);\n");
			source.append("send_data(text_buf, destination_component);\n");
			source.append("}\n");
			source.append("}\n\n");

			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(MessageFormat.format("public void reply(final {0}_template reply_template, final {1} destination_address) '{'\n", info.mJavaTypeName, portDefinition.addressName));
				source.append("if (!is_started) {\n");
				source.append("throw new TtcnError(MessageFormat.format(\"Replying to a signature on port {0}, which is not started.\", get_name()));\n");
				source.append("}\n");
				source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMOUT)) {");
				source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
				source.append("destination_address.log();\n");
				source.append("TitanCharString log_temp = TtcnLogger.end_event_log2str();\n");
				source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
				source.append("reply_template.log();\n");
				source.append("TtcnLogger.log_procport_send(get_name(), TitanLoggerApi.Port__oper.enum_type.reply__op, TitanComponent.SYSTEM_COMPREF, log_temp, TtcnLogger.end_event_log2str());\n");
				source.append("}\n");
				source.append(MessageFormat.format("final {0}_reply reply_temp = reply_template.create_reply();\n", info.mJavaTypeName));
				source.append("outgoing_reply(reply_temp, destination_address);\n");
				source.append("}\n\n");
			}

			source.append(MessageFormat.format("public void reply(final {0}_template reply_template) '{'\n", info.mJavaTypeName));
			source.append("reply(reply_template, new TitanComponent(get_default_destination()));\n");
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the raise function for a signature
	 *
	 * @param source where the source code is to be generated.
	 * @param info information about the signature type.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateRaiseFunction(final StringBuilder source, final procedureSignatureInfo info, final PortDefinition portDefinition) {
		if (info.hasExceptions) {
			source.append(MessageFormat.format("public void raise(final {0}_exception raise_exception, final TitanComponent destination_component) '{'\n", info.mJavaTypeName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Raising an exception on port {0}, which is not started.\", get_name()));\n");
			source.append("}\n");
			source.append("if (!destination_component.isBound()) {\n");
			source.append("throw new TtcnError(\"Unbound component reference in the to clause of raise operation.\");\n");
			source.append("}\n\n");

			source.append("final TtcnLogger.Severity log_sev = destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF) ? TtcnLogger.Severity.PORTEVENT_PMOUT : TtcnLogger.Severity.PORTEVENT_PCOUT;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
			source.append("raise_exception.log();\n");
			source.append("TtcnLogger.log_procport_send(get_name(), TitanLoggerApi.Port__oper.enum_type.exception__op, destination_component.getComponent(), new TitanCharString(\"\"), TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("if (destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF)) {\n");
			if (portDefinition.testportType == TestportType.INTERNAL) {
				source.append("throw new TtcnError(MessageFormat.format(\"Internal port {0} cannot raise an exception to system.\", get_name()));\n");
			} else if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append("outgoing_raise(raise_exception, null);\n");
			} else {
				source.append("outgoing_raise(raise_exception);\n");
			}
			source.append("} else {\n");
			source.append("Text_Buf text_buf = new Text_Buf();\n");
			source.append(MessageFormat.format("prepare_exception(text_buf, \"{0}\");\n", info.mDisplayName));
			source.append("raise_exception.encode_text(text_buf);\n");
			source.append("send_data(text_buf, destination_component);\n");
			source.append("}\n");
			source.append("}\n\n");

			if (portDefinition.testportType == TestportType.ADDRESS) {
				source.append(MessageFormat.format("public void raise(final {0}_exception raise_exception, final {1} destination_address) '{'\n", info.mJavaTypeName, portDefinition.addressName));
				source.append("if (!is_started) {\n");
				source.append("throw new TtcnError(MessageFormat.format(\"Raising an exception on port {0}, which is not started.\", get_name()));\n");
				source.append("}\n");
				source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMOUT)) {");
				source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
				source.append("destination_address.log();\n");
				source.append("TitanCharString log_temp = TtcnLogger.end_event_log2str();\n");
				source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMOUT);\n");
				source.append("raise_exception.log();\n");
				source.append("TtcnLogger.log_procport_send(get_name(), TitanLoggerApi.Port__oper.enum_type.exception__op, TitanComponent.SYSTEM_COMPREF, log_temp, TtcnLogger.end_event_log2str());\n");
				source.append("}\n");
				source.append("outgoing_raise(raise_exception, destination_address);\n");
				source.append("}\n\n");
			}

			source.append(MessageFormat.format("public void raise(final {0}_exception raise_exception) '{'\n", info.mJavaTypeName));
			source.append("raise(raise_exception, new TitanComponent(get_default_destination()));\n");
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the generic getcall or check(getcall) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateGenericGetcall(final StringBuilder source, final PortDefinition portDefinition, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_getcall" : "getcall";
		final String printedFunctionName = isCheck ? "Check-getcall" : "Getcall";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template sender_template, final {1} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, senderType));
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("Procedure_queue_item head = procedure_queue.getFirst();\n");
		if (isAddress) {
			source.append("if (head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PMUNSUCC, MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue is not the system.\", get_name()));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name()));\n", printedFunctionName));
			source.append("} else if (!sender_template.match(head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMUNSUCC);\n");
			source.append("sender_template.log_match(head.sender_address, false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), TitanComponent.SYSTEM_COMPREF, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		} else {
			source.append("if (!sender_template.match(head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event_str(MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: \", get_name()));\n");
			source.append("sender_template.log_match(new TitanComponent(head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("}\n");
		}
		source.append("switch(head.item_selection) {\n");
		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			source.append(MessageFormat.format("case CALL_{0}:\n", i));
		}

		source.append("{\n");
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(head.sender_component);\n");
		}
		source.append("}\n");
		if(isAddress) {
			source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_PMSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMIN)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.call__op, head.sender_component, {0}, new TitanCharString(\"\"), msg_head_count+1);\n", isCheck ? "true":"false"));
			source.append("}\n");
		} else {
			source.append("TtcnLogger.log(head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMSUCCESS : TtcnLogger.Severity.MATCHING_PCSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component ==  TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_PMIN : TtcnLogger.Severity.PORTEVENT_PCIN;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.call__op, head.sender_component, {0} ,new TitanCharString(\"\"), msg_head_count+1);\n", isCheck ? "true" : "false"));
			source.append("}\n");
		}
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append(MessageFormat.format("TtcnLogger.log({0}, MessageFormat.format(\"Matching on port '{'0'}' failed: First entity in the queue is not a call.\", get_name()));\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC"));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the getcall or check(getcall) function for a signature type
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param portDefinition the definition of the port.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateTypedGetcall(final StringBuilder source, final PortDefinition portDefinition, final int index, final procedureSignatureInfo info, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_getcall" : "getcall";
		final String printedFunctionName = isCheck ? "Check-getcall" : "Getcall";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template getcall_template, final {2}_template sender_template, final {1}_call_redirect param_ref, final {2} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, info.mJavaTypeName, senderType));
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("Procedure_queue_item head = procedure_queue.getFirst();\n");
		if (isAddress) {
			source.append("if (head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__is__not__system, new TitanCharString(\"\"));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name()));\n", printedFunctionName));
			source.append("} else if (!sender_template.match(head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMUNSUCC);\n");
			source.append("sender_template.log_match(head.sender_address, false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), TitanComponent.SYSTEM_COMPREF, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		} else {
			source.append("if (!sender_template.match(head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("sender_template.log_match(new TitanComponent(head.sender_component), false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		}
		source.append(MessageFormat.format(" else if (head.item_selection != proc_selection.CALL_{0}) '{'\n", index));
		source.append(MessageFormat.format("TtcnLogger.log({0}, MessageFormat.format(\"Matching on port '{'0'}' failed: The first entity in the queue is not a call for signature {1}.\", get_name()));\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC", portDefinition.displayName));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!getcall_template.match_call(head.call_{0}, true)) '{'\n", index));
		source.append(MessageFormat.format("final TtcnLogger.Severity log_sev = {0};\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC"));
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(log_sev);\n");
		source.append(MessageFormat.format("getcall_template.log_match_call(head.call_{0}, false);\n", index));
		source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.parameters__of__call__do__not__match__template, TtcnLogger.end_event_log2str());\n");
		source.append("}\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("} else {\n");
		source.append(MessageFormat.format("param_ref.set_parameters(head.call_{0});\n", index));
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(head.sender_component);\n");
		}
		source.append("}\n");
		generate_proc_incoming_data_logging(source, "call", "getcall_template.log_match_call", isAddress, isCheck, index);
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the generic getreply or check(getreply) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateGenericGetreply(final StringBuilder source, final PortDefinition portDefinition, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_reply" : "getreply";
		final String printedFunctionName = isCheck ? "Check-getreply" : "Getreply";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template sender_template, final {1} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, senderType));
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("Procedure_queue_item head = procedure_queue.getFirst();\n");
		if (isAddress) {
			source.append("if (head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PMUNSUCC, MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue is not the system.\", get_name()));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name()));\n", printedFunctionName));
			source.append("} else if (!sender_template.match(head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMUNSUCC);\n");
			source.append("sender_template.log_match(head.sender_address, false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), TitanComponent.SYSTEM_COMPREF, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("}\n");
		} else {
			source.append("if (!sender_template.match(head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event_str(MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: \", get_name()));\n");
			source.append("sender_template.log_match(new TitanComponent(head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("}\n");
		}
		source.append("switch(head.item_selection) {\n");
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			if (!portDefinition.outProcedures.get(i).isNoBlock) {
				source.append(MessageFormat.format("case REPLY_{0}:\n", i));
			}
		}

		source.append("{\n");
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(head.sender_component);\n");
		}
		source.append("}\n");
		if(isAddress) {
			source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_PMSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMIN)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.reply__op, head.sender_component, {0}, new TitanCharString(\"\"), msg_head_count+1);\n", isCheck ? "true" : "false"));
			source.append("}\n");
		} else {
			source.append("TtcnLogger.log(head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMSUCCESS : TtcnLogger.Severity.MATCHING_PCSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component ==  TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_PMIN : TtcnLogger.Severity.PORTEVENT_PCIN;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.reply__op, head.sender_component, {0} ,new TitanCharString(\"\"), msg_head_count+1);\n", isCheck ? "true" : "false"));
			source.append("}\n");
		}
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append(MessageFormat.format("TtcnLogger.log({0}, MessageFormat.format(\"Matching on port '{'0'}' failed: First entity in the queue is not a reply.\", get_name()));\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC"));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the getreply or check(getreply) function for a signature type
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param portDefinition the definition of the port.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateTypedGetreply(final StringBuilder source, final PortDefinition portDefinition, final int index, final procedureSignatureInfo info, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_getreply" : "getreply";
		final String printedFunctionName = isCheck ? "Check-getreply" : "Getreply";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template getreply_template, final {2}_template sender_template, final {1}_reply_redirect param_ref, final {2} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, info.mJavaTypeName, senderType));
		if (info.hasReturnValue) {
			source.append("if (getreply_template.return_value().get_selection() == template_sel.ANY_OR_OMIT) {\n");
			source.append(MessageFormat.format("throw new TtcnError(\"{0} operation using '*' as return value matching template\");\n", printedFunctionName));
			source.append("}\n");
		}
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("Procedure_queue_item head = procedure_queue.getFirst();\n");
		if (isAddress) {
			source.append("if (head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PMUNSUCC, MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue is not the system.\", get_name()));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name()));\n", printedFunctionName));
			source.append("} else if (!sender_template.match(head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMUNSUCC);\n");
			source.append("sender_template.log_match(head.sender_address, false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), TitanComponent.SYSTEM_COMPREF, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		} else {
			source.append("if (!sender_template.match(head.sender_component, false)) {\n");

			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event_str(MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: \", get_name()));\n");
			source.append("sender_template.log_match(new TitanComponent(head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		}
		source.append(MessageFormat.format(" else if (head.item_selection != proc_selection.REPLY_{0}) '{'\n", index));
		source.append(MessageFormat.format("TtcnLogger.log({0}, MessageFormat.format(\"Matching on port '{'0'}' failed: The first entity in the queue is not a reply for signature {1}.\", get_name()));\n ",  isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC",portDefinition.displayName));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!getreply_template.match_reply(head.reply_{0}, true)) '{'\n", index));
		source.append(MessageFormat.format("final TtcnLogger.Severity log_sev = {0};\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC"));
		source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		source.append("TtcnLogger.begin_event(log_sev);\n");
		source.append(MessageFormat.format("getreply_template.log_match_reply(head.reply_{0}, false);\n", index));
		source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.parameters__of__reply__do__not__match__template, TtcnLogger.end_event_log2str());\n");
		source.append("}\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("} else {\n");
		source.append(MessageFormat.format("param_ref.set_parameters(head.reply_{0});\n", index));
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(head.sender_component);\n");
		}
		source.append("}\n");
		generate_proc_incoming_data_logging(source, "reply", "getreply_template.log_match_reply", isAddress, isCheck, index);
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the generic get_exception or check(catch) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateGenericGetexception(final StringBuilder source, final PortDefinition portDefinition, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_catch" : "get_exception";
		final String printedFunctionName = isCheck ? "Check-catch" : "Catch";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template sender_template, final {1} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, senderType));
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("Procedure_queue_item head = procedure_queue.getFirst();\n");
		if (isAddress) {
			source.append("if (head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PMUNSUCC, MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue is not the system.\", get_name()));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name()));\n", printedFunctionName));
			source.append("} else if (!sender_template.match(head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMUNSUCC);\n");
			source.append("sender_template.log_match(head.sender_address, false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), TitanComponent.SYSTEM_COMPREF, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("}\n");
		} else {
			source.append("if (!sender_template.match(head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event_str(MessageFormat.format(\"Matching on port {0} failed: Sender of the first entity in the queue does not match the from clause: \", get_name()));\n");
			source.append("sender_template.log_match(new TitanComponent(head.sender_component), false);\n");
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("}\n");
		}
		source.append("switch(head.item_selection) {\n");
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			if (portDefinition.outProcedures.get(i).hasExceptions) {
				source.append(MessageFormat.format("case EXCEPTION_{0}:\n", i));
			}
		}

		source.append("{\n");
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(head.sender_component);\n");
		}
		source.append("}\n");
		if(isAddress) {
			source.append("TtcnLogger.log(TtcnLogger.Severity.MATCHING_PMSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMIN)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.exception__op, head.sender_component, {0}, new TitanCharString(\"\"), msg_head_count+1);\n", isCheck ? "true" : "false"));
			source.append("}\n");
		} else {
			source.append("TtcnLogger.log(head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMSUCCESS : TtcnLogger.Severity.MATCHING_PCSUCCESS,  MessageFormat.format(\"Matching on port {0} succeeded.\", get_name()));\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component ==  TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_PMIN : TtcnLogger.Severity.PORTEVENT_PCIN;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.exception__op, head.sender_component, {0} ,new TitanCharString(\"\"), msg_head_count+1);\n", isCheck ? "true" : "false"));
			source.append("}\n");
		}
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append(MessageFormat.format("TtcnLogger.log({0}, MessageFormat.format(\"Matching on port '{'0'}' failed: First entity in the queue is not an exception.\", get_name()));\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC"));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the get_exception or check(catch) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param portDefinition the definition of the port.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param isCheck generate the check or the non-checking version.
	 * @param isAddress generate for address or not?
	 * */
	private static void generateTypedGetexception(final StringBuilder source, final PortDefinition portDefinition, final int index, final procedureSignatureInfo info, final boolean isCheck, final boolean isAddress) {
		final String functionName = isCheck ? "check_catch" : "get_exception";
		final String printedFunctionName = isCheck ? "Check-catch" : "Catch";
		final String senderType = isAddress ? portDefinition.addressName : "TitanComponent";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_exception_template catch_template, final {2}_template sender_template, final {2} sender_pointer, final Index_Redirect index_redirect) '{'\n", functionName, info.mJavaTypeName, senderType));
		if (info.hasReturnValue) {
			source.append("if (catch_template.is_any_or_omit()) {\n");
			source.append("throw new TtcnError(\"Catch operation using '*' as matching template\");\n");
			source.append("}\n");
		}
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("TtcnLogger.log_str(TtcnLogger.Severity.MATCHING_PROBLEM, MessageFormat.format(\"Matching on port {0} failed: Port is not started and the queue is empty.\", get_name()));\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("Procedure_queue_item head = procedure_queue.getFirst();\n");
		if (isAddress) {
			source.append("if (head.sender_component != TitanComponent.SYSTEM_COMPREF) {\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__is__not__system, new TitanCharString(\"\"));\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append("} else if (head.sender_address == null) {\n");
			source.append(MessageFormat.format("throw new TtcnError(MessageFormat.format(\"{0} operation on port '{'0'}' requires the address of the sender, which was not given by the test port.\", get_name()));\n", printedFunctionName));
			source.append("} else if (!sender_template.match(head.sender_address, false)) {\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMUNSUCC);\n");
			source.append("sender_template.log_match(head.sender_address, false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		} else {
			source.append("if (!sender_template.match(head.sender_component, false)) {\n");
			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("sender_template.log_match(new TitanComponent(head.sender_component), false);\n");
			source.append("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), head.sender_component, TitanLoggerApi.MatchingFailureType_reason.enum_type.sender__does__not__match__from__clause, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append('}');
		}
		source.append(MessageFormat.format(" else if (head.item_selection != proc_selection.EXCEPTION_{0}) '{'\n", index));
		source.append(MessageFormat.format("TtcnLogger.log_matching_failure(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), {0}, TitanLoggerApi.MatchingFailureType_reason.enum_type.not__an__exception__for__signature, new TitanCharString(\"{1} \"));\n", isAddress ? "TitanComponent.SYSTEM_COMPREF" : "head.sender_component", info.mDisplayName));
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!catch_template.match(head.exception_{0}, true)) '{'\n", index));
		if(isAddress) {
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMUNSUCC)) {\n");
		} else {
			source.append("final TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMUNSUCC : TtcnLogger.Severity.MATCHING_PCUNSUCC;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
		}
		source.append(MessageFormat.format("TtcnLogger.begin_event({0});\n", isAddress ? "TtcnLogger.Severity.MATCHING_PMUNSUCC" : "log_sev"));
		source.append(MessageFormat.format("catch_template.log_match(head.exception_{0}, false);\n", index));
		source.append("TtcnLogger.end_event_log2str();\n");
		source.append("}\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("} else {\n");
		source.append("//FIXME set param_ref\n");
		source.append("if (sender_pointer != null) {\n");
		if (isAddress) {
			source.append("sender_pointer.assign(head.sender_address);\n");
		} else {
			source.append("sender_pointer.assign(head.sender_component);\n");
		}
		source.append("}\n");
		generate_proc_incoming_data_logging(source, "exception", "catch_template.log_match", isAddress, isCheck, index);
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the type incoming call function.
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateTypedIcomingCall(final StringBuilder source, final int index, final procedureSignatureInfo info, final PortDefinition portDefinition) {
		source.append(MessageFormat.format("protected void incoming_call(final {0}_call incoming_par, final int sender_component", info.mJavaTypeName));
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format(", final {0} sender_address", portDefinition.addressName));
		}
		source.append(") {\n" );
		source.append("if (!is_started) {\n" );
		source.append("throw new TtcnError(MessageFormat.format(\"Port {0} is not started but a call has arrived on it.\", get_name()));\n");
		source.append("}\n" );
		source.append("if(TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PQUEUE)) {\n");
		if(portDefinition.testportType == TestportType.ADDRESS) {
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PQUEUE);\n");
			source.append("TtcnLogger.log_char('(');\n");
			source.append("sender_address.log();\n");
			source.append("TtcnLogger.log_char(')');\n");
			source.append("TitanCharString tempLog = TtcnLogger.end_event_log2str();\n");
		}
		source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PQUEUE);\n");
		source.append("TtcnLogger.log_char(' ');\n");
		source.append("incoming_par.log();\n");
		source.append("TtcnLogger.log_port_queue(TitanLoggerApi.Port__Queue_operation.enum_type.enqueue__call, get_name(), sender_component, proc_tail_count, ");
		if(portDefinition.testportType == TestportType.ADDRESS) {
			source.append("tempLog, TtcnLogger.end_event_log2str());\n");
		} else {
			source.append("new TitanCharString(\"\"), TtcnLogger.end_event_log2str());\n");
		}
		source.append("}\n");
		source.append("Procedure_queue_item newItem = new Procedure_queue_item();\n" );
		source.append(MessageFormat.format("newItem.item_selection = proc_selection.CALL_{0};\n", index));
		source.append(MessageFormat.format("newItem.call_{0} = new {1}_call(incoming_par);\n", index, info.mJavaTypeName));
		source.append("newItem.sender_component = sender_component;\n" );
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append("if (sender_address != null) {\n" );
			source.append(MessageFormat.format("newItem.sender_address = new {0}(sender_address);\n", portDefinition.addressName));
			source.append("} else {\n" );
			source.append("newItem.sender_address = null;\n" );
			source.append("}\n" );
		}
		source.append("procedure_queue.add(newItem);\n" );
		source.append("}\n\n");

		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format("protected void incoming_call(final {0}_call incoming_par, final int sender_component) '{'\n", info.mJavaTypeName));
			source.append("incoming_call(incoming_par, TitanComponent.SYSTEM_COMPREF, null);\n" );
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("protected void incoming_call(final {0}_call incoming_par) '{'\n", info.mJavaTypeName));
		source.append("incoming_call(incoming_par, TitanComponent.SYSTEM_COMPREF);\n" );
		source.append("}\n\n");
	}

	/**
	 * This function generates the type incoming reply function.
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateTypedIcomingReply(final StringBuilder source, final int index, final procedureSignatureInfo info, final PortDefinition portDefinition) {
		source.append(MessageFormat.format("protected void incoming_reply(final {0}_reply incoming_par, final int sender_component", info.mJavaTypeName));
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format(", final {0} sender_address", portDefinition.addressName));
		}
		source.append(") {\n" );
		source.append("if (!is_started) {\n" );
		source.append("throw new TtcnError(MessageFormat.format(\"Port {0} is not started but a reply has arrived on it.\", get_name()));\n");
		source.append("}\n" );
		source.append("if(TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PQUEUE)) {\n");
		if(portDefinition.testportType == TestportType.ADDRESS) {
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PQUEUE);\n");
			source.append("TtcnLogger.log_char('(');\n");
			source.append("sender_address.log();\n");
			source.append("TtcnLogger.log_char(')');\n");
			source.append("TitanCharString tempLog = TtcnLogger.end_event_log2str();\n");
		}
		source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PQUEUE);\n");
		source.append("TtcnLogger.log_char(' ');\n");
		source.append("incoming_par.log();\n");
		source.append("TtcnLogger.log_port_queue(TitanLoggerApi.Port__Queue_operation.enum_type.enqueue__reply, get_name(), sender_component, proc_tail_count, ");
		if(portDefinition.testportType == TestportType.ADDRESS) {
			source.append("tempLog, TtcnLogger.end_event_log2str());\n");
		} else {
			source.append("new TitanCharString(\"\"), TtcnLogger.end_event_log2str());\n");
		}
		source.append("}\n");
		source.append("Procedure_queue_item newItem = new Procedure_queue_item();\n" );
		source.append(MessageFormat.format("newItem.item_selection = proc_selection.REPLY_{0};\n", index));
		source.append(MessageFormat.format("newItem.reply_{0} = new {1}_reply(incoming_par);\n", index, info.mJavaTypeName));
		source.append("newItem.sender_component = sender_component;\n" );
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append("if (sender_address != null) {\n" );
			source.append(MessageFormat.format("newItem.sender_address = new {0}(sender_address);\n", portDefinition.addressName));
			source.append("} else {\n" );
			source.append("newItem.sender_address = null;\n" );
			source.append("}\n" );
		}
		source.append("procedure_queue.add(newItem);\n" );
		source.append("}\n\n");

		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format("protected void incoming_reply(final {0}_reply incoming_par, final int sender_component) '{'\n", info.mJavaTypeName));
			source.append("incoming_reply(incoming_par, TitanComponent.SYSTEM_COMPREF, null);\n" );
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("protected void incoming_reply(final {0}_reply incoming_par) '{'\n", info.mJavaTypeName));
		source.append("incoming_reply(incoming_par, TitanComponent.SYSTEM_COMPREF);\n" );

		source.append("}\n\n");
	}

	/**
	 * This function generates the type incoming exception function.
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateTypedIcomingException(final StringBuilder source, final int index, final procedureSignatureInfo info, final PortDefinition portDefinition) {
		source.append(MessageFormat.format("protected void incoming_exception(final {0}_exception incoming_par, final int sender_component", info.mJavaTypeName));
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format(", final {0} sender_address", portDefinition.addressName));
		}
		source.append(") {\n" );
		source.append("if (!is_started) {\n" );
		source.append("throw new TtcnError(MessageFormat.format(\"Port {0} is not started but an exception has arrived on it.\", get_name()));\n");
		source.append("}\n" );
		source.append("if(TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PQUEUE)) {\n");
		if(portDefinition.testportType == TestportType.ADDRESS) {
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PQUEUE);\n");
			source.append("TtcnLogger.log_char('(');\n");
			source.append("sender_address.log();\n");
			source.append("TtcnLogger.log_char(')');\n");
			source.append("TitanCharString tempLog = TtcnLogger.end_event_log2str();\n");
		}
		source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PQUEUE);\n");
		source.append("TtcnLogger.log_char(' ');\n");
		source.append("incoming_par.log();\n");
		source.append("TtcnLogger.log_port_queue(TitanLoggerApi.Port__Queue_operation.enum_type.enqueue__exception, get_name(), sender_component, proc_tail_count, ");
		if(portDefinition.testportType == TestportType.ADDRESS) {
			source.append("tempLog, TtcnLogger.end_event_log2str());\n");
		} else {
			source.append("new TitanCharString(\"\"), TtcnLogger.end_event_log2str());\n");
		}
		source.append("}\n");
		source.append("Procedure_queue_item newItem = new Procedure_queue_item();\n" );
		source.append(MessageFormat.format("newItem.item_selection = proc_selection.EXCEPTION_{0};\n", index));
		source.append(MessageFormat.format("newItem.exception_{0} = new {1}_exception(incoming_par);\n", index, info.mJavaTypeName));
		source.append("newItem.sender_component = sender_component;\n" );
		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append("if (sender_address != null) {\n" );
			source.append(MessageFormat.format("newItem.sender_address = new {0}(sender_address);\n", portDefinition.addressName));
			source.append("} else {\n" );
			source.append("newItem.sender_address = null;\n" );
			source.append("}\n" );
		}
		source.append("procedure_queue.add(newItem);\n" );
		source.append("}\n\n");

		if (portDefinition.testportType == TestportType.ADDRESS) {
			source.append(MessageFormat.format("protected void incoming_exception(final {0}_exception incoming_par, final int sender_component) '{'\n", info.mJavaTypeName));
			source.append("incoming_exception(incoming_par, TitanComponent.SYSTEM_COMPREF, null);\n" );
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("protected void incoming_exception(final {0}_exception incoming_par) '{'\n", info.mJavaTypeName));
		source.append("incoming_exception(incoming_par, TitanComponent.SYSTEM_COMPREF);\n" );
		source.append("}\n\n");
	}

	/**
	 * This function generates the process_call function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateProcessCall(final StringBuilder source, final PortDefinition portDefinition) {
		source.append("protected boolean process_call(final String signature_name, final Text_Buf incoming_buf, final int sender_component) {\n");
		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.inProcedures.get(i);

			if (i != 0) {
				source.append(" } else ");
			}
			source.append(MessageFormat.format("if (\"{0}\".equals(signature_name)) '{'\n", info.mDisplayName));
			source.append(MessageFormat.format("{0}_call incoming_par = new {0}_call();\n", info.mJavaTypeName));
			source.append("incoming_par.decode_text(incoming_buf);\n");
			source.append("incoming_call(incoming_par, sender_component);\n");
			source.append("return true;\n");
		}


		source.append("} else {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n");
	}

	/**
	 * This function generates the process_reply function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateProcessReply(final StringBuilder source, final PortDefinition portDefinition) {
		source.append("protected boolean process_reply(final String signature_name, final Text_Buf incoming_buf, final int sender_component) {\n");
		boolean isFirst = true;
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			if (!info.isNoBlock) {
				if (!isFirst) {
					source.append(" } else ");
				}
				isFirst = false;
				source.append(MessageFormat.format("if (\"{0}\".equals(signature_name)) '{'\n", info.mDisplayName));
				source.append(MessageFormat.format("{0}_reply incoming_par = new {0}_reply();\n", info.mJavaTypeName));
				source.append("incoming_par.decode_text(incoming_buf);\n");
				source.append("incoming_reply(incoming_par, sender_component);\n");
				source.append("return true;\n");
			}
		}


		source.append("} else {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n");
	}

	/**
	 * This function generates the process_exception function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * */
	private static void generateProcessException(final StringBuilder source, final PortDefinition portDefinition) {
		source.append("protected boolean process_exception(final String signature_name, final Text_Buf incoming_buf, final int sender_component) {\n");
		boolean isFirst = true;
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			final procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			if (info.hasExceptions) {
				if (!isFirst) {
					source.append(" } else ");
				}
				isFirst = false;
				source.append(MessageFormat.format("if (\"{0}\".equals(signature_name)) '{'\n", info.mDisplayName));
				source.append(MessageFormat.format("{0}_exception incoming_par = new {0}_exception();\n", info.mJavaTypeName));
				source.append("incoming_par.decode_text(incoming_buf);\n");
				source.append("incoming_exception(incoming_par, sender_component);\n");
				source.append("return true;\n");
			}
		}


		source.append("} else {\n");
		source.append("return false;\n");
		source.append("}\n");
		source.append("}\n");
	}

	/**
	 * A utility function for generating code for the standalone version of
	 *  receive/trigger/getcall/getreply/catch/check/check-receive/check-getcall/check-getreply/check-catch/timeout/done/killed
	 *  statements.
	 *
	 * @param aData only used to update imports if needed.
	 * @param source where the source code is to be generated.
	 * @param statement the code generated for the statement as an expression.
	 * @param statementName the name of the statement for display in error message
	 * @param location the location of the statement to report errors to.
	 * */
	public static void generateCodeStandalone(final JavaGenData aData, final StringBuilder source, final String statement, final String statementName, final Location location) {
		aData.addBuiltinTypeImport("TitanAlt_Status");
		aData.addBuiltinTypeImport("TTCN_Default");
		aData.addBuiltinTypeImport("TtcnError");
		aData.addCommonLibraryImport("TTCN_Snapshot");

		final String tempLabel = aData.getTemporaryVariableName();

		source.append(MessageFormat.format("{0}: for( ; ; ) '{'\n", tempLabel));
		source.append("TitanAlt_Status alt_flag = TitanAlt_Status.ALT_UNCHECKED;\n");
		source.append("TitanAlt_Status default_flag = TitanAlt_Status.ALT_UNCHECKED;\n");
		source.append("TTCN_Snapshot.takeNew(false);\n");
		source.append("for( ; ; ) {\n");
		source.append("if (alt_flag != TitanAlt_Status.ALT_NO) {\n");

		source.append(MessageFormat.format("alt_flag = {0};\n", statement));

		source.append("if (alt_flag == TitanAlt_Status.ALT_YES) {\n");
		source.append("break;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("if (default_flag != TitanAlt_Status.ALT_NO) {\n");
		source.append("default_flag = TTCN_Default.tryAltsteps();\n");
		source.append("if (default_flag == TitanAlt_Status.ALT_YES || default_flag == TitanAlt_Status.ALT_BREAK) {\n");
		source.append("break;\n");
		source.append("} else if (default_flag == TitanAlt_Status.ALT_REPEAT) {\n");
		source.append(MessageFormat.format("continue {0};\n", tempLabel));
		source.append("}\n");
		source.append("}\n");
		source.append("if (alt_flag == TitanAlt_Status.ALT_NO && default_flag == TitanAlt_Status.ALT_NO) {\n");
		source.append(MessageFormat.format("throw new TtcnError(\"Stand-alone {0} statement failed in file {1}, line {2}.\");\n", statementName, location.getFile().getProjectRelativePath(), location.getLine()));
		source.append("}\n");
		source.append("TTCN_Snapshot.takeNew(true);\n");
		source.append("}\n");
		source.append("break;\n");
		source.append("}\n");
	}

	/**
	 * Generate code for logging
	 *
	 * Called from generateTypedGetcall, generateTypedgetreply, generateTypedexception
	 *
	 * @param source where the source code is to be generated
	 * @param opStr "call", "reply" or "exception"
	 * @param matchStr "catch_template.log_match", "getcall_template.log_match_call" or "getreply_template.log_match_reply"
	 * @param isAddress generate for address or not?
	 * @param isCheck generate the check or the non-checking version.
	 * @param index the index this signature type has in the selector.
	 */
	private static void generate_proc_incoming_data_logging(final StringBuilder source, final String opStr, final String matchStr, final boolean isAddress, final boolean isCheck, final int index) {
		String procOp = "";
		if ("call".equals(opStr)) {
			procOp = "call";
		} else if("reply".equals(opStr)) {
			procOp = "reply";
		} else if("exception".equals(opStr)) {
			procOp = "exception";
		}
		if(isAddress) {
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.MATCHING_PMSUCCESS)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.MATCHING_PMSUCCESS);\n");
			source.append(MessageFormat.format("{0}(head.{1}_{2},false);\n", matchStr, opStr, index));
			source.append("TtcnLogger.log_matching_success(TitanLoggerApi.PortType.enum_type.procedure__, get_name(), TitanComponent.SYSTEM_COMPREF, TtcnLogger.end_event_log2str());\n");
			source.append("}\n");
			source.append("if (TtcnLogger.log_this_event(TtcnLogger.Severity.PORTEVENT_PMIN)) {\n");
			source.append("TtcnLogger.begin_event(TtcnLogger.Severity.PORTEVENT_PMIN);\n");
			source.append(MessageFormat.format("head.{0}_{1}.log();\n", opStr, index));
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.{0}__op, head.sender_component, {1}, TtcnLogger.end_event_log2str(), msg_head_count+1);\n", procOp, isCheck ? "true" : "false"));
			source.append("}\n");
		} else {
			source.append("TtcnLogger.Severity log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.MATCHING_PMSUCCESS : TtcnLogger.Severity.MATCHING_PCSUCCESS;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append("TtcnLogger.log_event(MessageFormat.format(\"Matching on port {0} succeeded: \", get_name()));\n");
			source.append(MessageFormat.format("{0}(head.{1}_{2}, false);\n", matchStr, opStr, index));
			source.append("TtcnLogger.end_event();\n");
			source.append("}\n");
			source.append("log_sev = head.sender_component == TitanComponent.SYSTEM_COMPREF ? TtcnLogger.Severity.PORTEVENT_PMIN : TtcnLogger.Severity.PORTEVENT_PCIN;\n");
			source.append("if (TtcnLogger.log_this_event(log_sev)) {\n");
			source.append("TtcnLogger.begin_event(log_sev);\n");
			source.append(MessageFormat.format("head.{0}_{1}.log();\n", opStr, index));
			source.append(MessageFormat.format("TtcnLogger.log_procport_recv(get_name(), TitanLoggerApi.Port__oper.enum_type.{0}__op, head.sender_component, {1} ,TtcnLogger.end_event_log2str(), msg_head_count+1);\n", procOp, isCheck ? "true" : "false"));
			source.append("}\n");
		}
	}
}
