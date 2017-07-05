package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

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

		/**
		 * @param messageType: the string representing the value type of this message in the generated code.
		 * @param messageTemplate: the string representing the template type of this message in the generated code.
		 * */
		public messageTypeInfo(final String messageType, final String messageTemplate) {
			mJavaTypeName = messageType;
			mJavaTemplateName = messageTemplate;
		}
	}

	//FIXME comment
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

		/** The list of incoming messages */
		public ArrayList<messageTypeInfo> inMessages = new ArrayList<PortGenerator.messageTypeInfo>();

		/** The list of outgoing messages */
		public ArrayList<messageTypeInfo> outMessages = new ArrayList<PortGenerator.messageTypeInfo>();

		public ArrayList<procedureSignatureInfo> inProcedures = new ArrayList<PortGenerator.procedureSignatureInfo>();

		public ArrayList<procedureSignatureInfo> outProcedures = new ArrayList<PortGenerator.procedureSignatureInfo>();


		/** The type of the testport */
		public TestportType testportType;

		public PortDefinition(final String genName, final String displayName) {
			javaName = genName;
			this.displayName = displayName;
		}
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
		aData.addBuiltinTypeImport( "TitanPort" );
		aData.addBuiltinTypeImport( "TitanAlt_Status" );
		aData.addBuiltinTypeImport( "Base_Type" );

		generateDeclaration(source, portDefinition);

		for (int i = 0 ; i < portDefinition.outMessages.size(); i++) {
			messageTypeInfo outType = portDefinition.outMessages.get(i);

			generateSend(source, outType, portDefinition.testportType);
		}

		if (portDefinition.inMessages.size() > 0) {
			generateGenericReceive(source, false);
			generateGenericReceive(source, true);
			generateGenericTrigger(source);
		}

		// generic and simplified receive for experimentation
		for (int i = 0 ; i < portDefinition.inMessages.size(); i++) {
			messageTypeInfo inType = portDefinition.inMessages.get(i);

			generateTypedReceive(source, i, inType, false);
			generateTypedReceive(source, i, inType, true);
			generateTypeTrigger(source, i, inType);
			generateTypedIncomminMessage(source, i, inType, portDefinition.testportType);
		}

		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			procedureSignatureInfo info = portDefinition.outProcedures.get(i);

			generateCallFunction(source, info, portDefinition.testportType);
		}
		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			procedureSignatureInfo info = portDefinition.inProcedures.get(i);

			generateReplyFunction(source, info, portDefinition.testportType);
		}
		for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
			procedureSignatureInfo info = portDefinition.inProcedures.get(i);

			generateRaiseFunction(source, info, portDefinition.testportType);
		}

		//FIXME more complicated conditional
		if (portDefinition.testportType != TestportType.INTERNAL) {
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				procedureSignatureInfo info = portDefinition.outProcedures.get(i);

				source.append(MessageFormat.format("public abstract void outgoing_call(final {0}_call call_par);\n\n", info.mJavaTypeName));
			}
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				procedureSignatureInfo info = portDefinition.inProcedures.get(i);

				if (!info.isNoBlock) {
					source.append(MessageFormat.format("public abstract void outgoing_reply(final {0}_reply reply_par);\n\n", info.mJavaTypeName));
				}
			}
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				procedureSignatureInfo info = portDefinition.inProcedures.get(i);

				if (info.hasExceptions) {
					source.append(MessageFormat.format("public abstract void outgoing_raise(final {0}_exception raise_exception);\n\n", info.mJavaTypeName));
				}
			}
		}

		if (portDefinition.inProcedures.size() > 0) {
			generateGenericGetcall(source, portDefinition, false);
			generateGenericGetcall(source, portDefinition, true);

			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				procedureSignatureInfo info = portDefinition.inProcedures.get(i);

				generateTypedGetcall(source, i, info, false);
				generateTypedGetcall(source, i, info, true);
			}
		}

		//FIXME implement getreply
		//FIXME implement check_getreply
		//FIXME implement get_exception
		//FIXME implement check_catch

		//FIXME incoming_call
		//FIXME incoming_reply
		//FIXME incoming_exception
		//FIXME process_call
		//FIXME process_reply
		//FIXME process_exception
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
	
			source.append("private class MessageQueueItem {\n");
			source.append("message_selection item_selection;\n");
			source.append("// base type could be: ");
			for (int i = 0 ; i < portDefinition.inMessages.size(); i++) {
				messageTypeInfo inType = portDefinition.inMessages.get(i);
	
				if (i > 0) {
					source.append(", ");
				}
				source.append(inType.mJavaTypeName);
			}
			source.append("\n");
			source.append("Base_Type message;\n");
			source.append("int sender_component;\n");
			source.append("}\n");
	
			source.append("private LinkedList<MessageQueueItem> message_queue = new LinkedList<MessageQueueItem>();\n\n");

			source.append("private void remove_msg_queue_head() {\n");
			source.append("message_queue.removeFirst();\n");
			source.append("}\n\n");
		}

		boolean hasIncomingCall = portDefinition.inProcedures.size() > 0;
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

		boolean hasProcedureQueue = hasIncomingCall || hasIncomingReply || hasIncomingException;
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
	
			source.append("private class ProcedureQueueItem {\n");
			source.append("proc_selection item_selection;\n");
			source.append("// TODO check if an object would be enough \n");
			for (int i = 0 ; i < portDefinition.inProcedures.size(); i++) {
				source.append(MessageFormat.format("{0}_call call_{1};\n", portDefinition.inProcedures.get(i).mJavaTypeName, i));
			}
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				procedureSignatureInfo info = portDefinition.outProcedures.get(i);
				if (!info.isNoBlock) {
					source.append(MessageFormat.format("{0}_reply reply_{1};\n", info.mJavaTypeName, i));
				}
			}
			for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
				procedureSignatureInfo info = portDefinition.outProcedures.get(i);
				if (info.hasExceptions) {
					source.append(MessageFormat.format("{0}_exception exception_{1};\n", info.mJavaTypeName, i));
				}
			}
			source.append("int sender_component;\n");
			source.append("}\n");
			source.append("private LinkedList<ProcedureQueueItem> procedure_queue = new LinkedList<ProcedureQueueItem>();\n");
	
			source.append("private void remove_proc_queue_head() {\n");
			source.append("procedure_queue.removeFirst();\n");
			//FIXME add logging
			source.append("}\n\n");
		}

		source.append(MessageFormat.format("public {0}( final String portName) '{'\n", className));
		source.append("super(portName);\n");
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
	 * @param testportType the type of the testport.
	 * */
	private static void generateSend(final StringBuilder source, final messageTypeInfo outType, final TestportType testportType) {
		source.append(MessageFormat.format("public void send(final {0} send_par, final TitanComponent destination_component) '{'\n", outType.mJavaTypeName));
		source.append("if (!is_started) {\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Sending a message on port {0}, which is not started.\", getName()));\n");
		source.append("}\n");
		source.append("if (!destination_component.isBound()) {\n");
		source.append("throw new TtcnError(\"Unbound component reference in the to clause of send operation.\");\n");
		source.append("}\n");
		source.append("//FIXME logging\n");
		source.append("if (TitanBoolean.getNative(destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF))) {\n");
		if (testportType == TestportType.INTERNAL) {
			source.append("throw new TtcnError(MessageFormat.format(\"Message cannot be sent to system on internal port {0}.\", getName()));\n");
		} else {
			source.append("//FIXME get_default_destination\n");
			source.append("outgoing_send(send_par);\n");
		}
		source.append("} else {\n");
		source.append("//FIXME implement\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Sending messages on port {0}, is not yet supported.\", getName()));\n");
		source.append("}\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("public void send(final {0} send_par, final TitanComponent destination_component) '{'\n", outType.mJavaTemplateName));
		source.append(MessageFormat.format("final {0} send_par_value = send_par.valueOf();\n", outType.mJavaTypeName));
		source.append("send(send_par_value, destination_component);\n");
		source.append("}\n\n");

		// FIXME a bit more complex expression
		if (testportType != TestportType.INTERNAL) {
			source.append(MessageFormat.format("public abstract void outgoing_send(final {0} send_par);\n\n", outType.mJavaTypeName));
		}
	}

	/**
	 * This function generates the generic receive or check(receive) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param isCheck generate the check or the non-checking version.
	 * */
	private static void generateGenericReceive(final StringBuilder source, final boolean isCheck) {
		final String functionName = isCheck ? "check_receive" : "receive";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", functionName ));
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");

		source.append("MessageQueueItem my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("} else if (!TitanBoolean.getNative(sender_template.match(my_head.sender_component, false))) {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
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
	 * */
	private static void generateGenericTrigger(final StringBuilder source) {
		source.append("public TitanAlt_Status trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {\n");
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");

		source.append("MessageQueueItem my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("} else if (!TitanBoolean.getNative(sender_template.match(my_head.sender_component, false))) {\n");
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
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
		String typeValueName = inType.mJavaTypeName;
		String typeTemplateName = inType.mJavaTemplateName;
		final String functionName = isCheck ? "check_receive" : "receive";

		//FIXME there are actually more parameters
		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1} value_template, final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", functionName, typeTemplateName));
		source.append("if (value_template.getSelection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("throw new TtcnError(\"Receive operation using '*' as matching template\");\n");
		source.append("}\n");
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n\n");
		source.append("MessageQueueItem my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("} else if (!TitanBoolean.getNative(sender_template.match(my_head.sender_component, false))) {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0}) '{'\n", index));
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!(my_head.message instanceof {0})) '{'\n", typeValueName));
		source.append("//FIXME report error \n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!TitanBoolean.getNative(value_template.match(({0}) my_head.message))) '{'\n", typeValueName));
		source.append("//FIXME implement\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(" } else {\n");
		source.append("//FIXME implement, right now we just assume perfect match\n");
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
		String typeValueName = inType.mJavaTypeName;
		String typeTemplateName = inType.mJavaTemplateName;

		//FIXME there are actually more parameters
		source.append(MessageFormat.format("public TitanAlt_Status trigger(final {0} value_template, final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", typeTemplateName));
		source.append("if (value_template.getSelection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("throw new TtcnError(\"Trigger operation using '*' as matching template\");\n");
		source.append("}\n");
		source.append("if (message_queue.isEmpty()) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("}\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n\n");
		source.append("MessageQueueItem my_head = message_queue.getFirst();\n");
		source.append("if (my_head == null) {\n");
		source.append("if (is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("} else if (!TitanBoolean.getNative(sender_template.match(my_head.sender_component, false))) {\n");
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0}) '{'\n", index));
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(MessageFormat.format("'}' else if (!(my_head.message instanceof {0})) '{'\n", typeValueName));
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(MessageFormat.format("'}' else if (!TitanBoolean.getNative(value_template.match(({0}) my_head.message))) '{'\n", typeValueName));
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(" } else {\n");
		source.append("//FIXME implement, right now we just assume perfect match\n");
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
	 * @param testportType the type of the testport.
	 * */
	private static void generateTypedIncomminMessage(final StringBuilder source, final int index, final messageTypeInfo inType, final TestportType testportType) {
		String typeValueName = inType.mJavaTypeName;

		source.append(MessageFormat.format("private void incoming_message(final {0} incoming_par, final int sender_component) '{'\n", typeValueName));
		source.append("if (!is_started) {\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Port {0} is not started but a message has arrived on it.\", getName()));\n");
		source.append("}\n");
		source.append("MessageQueueItem new_item = new MessageQueueItem();\n");
		source.append(MessageFormat.format("new_item.item_selection = message_selection.MESSAGE_{0};\n", index));
		source.append(MessageFormat.format("new_item.message = new {0}(incoming_par);\n", typeValueName));
		source.append("new_item.sender_component = sender_component;\n");
		source.append("message_queue.addLast(new_item);\n");
		source.append("}\n\n");

		if (testportType != TestportType.INTERNAL) {
			source.append(MessageFormat.format("protected void incoming_message(final {0} incoming_par) '{'\n", typeValueName));
			source.append("incoming_message(incoming_par, TitanComponent.SYSTEM_COMPREF);\n");
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the call function for a signature
	 *
	 * @param source where the source code is to be generated.
	 * @param info information about the signature type.
	 * @param testportType the type of the testport.
	 * */
	private static void generateCallFunction(final StringBuilder source, final procedureSignatureInfo info, final TestportType testportType) {
		source.append(MessageFormat.format("public void call(final {0}_template call_template, final TitanComponent destination_component) '{'\n", info.mJavaTypeName));
		source.append("if (!is_started) {\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Calling a signature on port {0}, which is not started.\", getName()));\n");
		source.append("}\n");
		source.append("if (!destination_component.isBound()) {\n");
		source.append("throw new TtcnError(\"Unbound component reference in the to clause of call operation.\");\n");
		source.append("}\n\n");

		source.append(MessageFormat.format("final {0}_call call_temp = call_template.create_call();\n", info.mJavaTypeName));
		source.append("//FIXME add logging\n");
		source.append("if (TitanBoolean.getNative(destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF))) {\n");
		if (testportType == TestportType.INTERNAL) {
			source.append("throw new TtcnError(MessageFormat.format(\"Internal port {0} cannot send call to system.\", getName()));\n");
		} else {
			source.append("//FIXME get_default_destination\n");
			source.append("outgoing_call(call_temp);\n");
		}
		
		source.append("} else {\n");
		source.append("//FIXME implement\n");
		source.append("throw new TtcnError(MessageFormat.format(\"Calling a signature on port {0}, is not yet supported.\", getName()));\n");
		source.append("}\n");
		source.append("}\n");
		source.append(MessageFormat.format("public void call(final {0}_template call_template) '{'\n", info.mJavaTypeName));
		source.append("//FIXME implement\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the reply function for a signature
	 *
	 * @param source where the source code is to be generated.
	 * @param info information about the signature type.
	 * @param testportType the type of the testport.
	 * */
	private static void generateReplyFunction(final StringBuilder source, final procedureSignatureInfo info, final TestportType testportType) {
		if (!info.isNoBlock) {
			source.append(MessageFormat.format("public void reply(final {0}_template reply_template, final TitanComponent destination_component) '{'\n", info.mJavaTypeName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Replying to a signature on port {0}, which is not started.\", getName()));\n");
			source.append("}\n");
			source.append("if (!destination_component.isBound()) {\n");
			source.append("throw new TtcnError(\"Unbound component reference in the to clause of reply operation.\");\n");
			source.append("}\n\n");

			source.append(MessageFormat.format("final {0}_reply reply_temp = reply_template.create_reply();\n", info.mJavaTypeName));
			source.append("//FIXME add logging\n");
			source.append("if (TitanBoolean.getNative(destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF))) {\n");
			if (testportType == TestportType.INTERNAL) {
				source.append("throw new TtcnError(MessageFormat.format(\"Internal port {0} cannot reply to system.\", getName()));\n");
			} else {
				source.append("//FIXME get_default_destination\n");
				source.append("outgoing_reply(reply_temp);\n");
			}
			source.append("} else {\n");
			source.append("//FIXME implement\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Replying to a signature on port {0}, is not yet supported.\", getName()));\n");
			source.append("}\n");
			source.append("}\n");

			source.append(MessageFormat.format("public void reply(final {0}_template reply_template) '{'\n", info.mJavaTypeName));
			source.append("//FIXME implement\n");
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the raise function for a signature
	 *
	 * @param source where the source code is to be generated.
	 * @param info information about the signature type.
	 * @param testportType the type of the testport.
	 * */
	private static void generateRaiseFunction(final StringBuilder source, final procedureSignatureInfo info, final TestportType testportType) {
		if (info.hasExceptions) {
			source.append(MessageFormat.format("public void raise(final {0}_exception raise_exception, final TitanComponent destination_component) '{'\n", info.mJavaTypeName));

			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Raising an exception on port {0}, which is not started.\", getName()));\n");
			source.append("}\n");
			source.append("if (!destination_component.isBound()) {\n");
			source.append("throw new TtcnError(\"Unbound component reference in the to clause of raise operation.\");\n");
			source.append("}\n\n");

			source.append("//FIXME add logging\n");
			source.append("if (TitanBoolean.getNative(destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF))) {\n");
			if (testportType == TestportType.INTERNAL) {
				source.append("throw new TtcnError(MessageFormat.format(\"Internal port {0} cannot raise an exception to system.\", getName()));\n");
			} else {
				source.append("//FIXME get_default_destination\n");
				source.append("outgoing_raise(raise_exception);\n");
			}
			source.append("} else {\n");
			source.append("//FIXME implement\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Raising an exception on port {0}, is not yet supported.\", getName()));\n");
			source.append("}\n");
			source.append("}\n");

			source.append(MessageFormat.format("public void raise(final {0}_exception raise_exception) '{'\n", info.mJavaTypeName));
			source.append("//FIXME implement\n");
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the generic getcall or check(getcall) function.
	 *
	 * @param source where the source code is to be generated.
	 * @param portDefinition the definition of the port.
	 * @param isCheck generate the check or the non-checking version.
	 * */
	private static void generateGenericGetcall(final StringBuilder source, final PortDefinition portDefinition, final boolean isCheck) {
		final String functionName = isCheck ? "check_getcall" : "getcall";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", functionName));
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("ProcedureQueueItem head = procedure_queue.getFirst();\n");
		source.append("if (!TitanBoolean.getNative(sender_template.match(head.sender_component, false))) {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("switch(head.item_selection) {\n");
		for (int i = 0 ; i < portDefinition.outProcedures.size(); i++) {
			source.append(MessageFormat.format("case CALL_{0}:\n", i));
		}

		source.append("{\n");
		source.append("//FIXME logging\n");
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("default:\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the getcall or check(getcall) function for a signature type
	 *
	 * @param source where the source code is to be generated.
	 * @param index the index this signature type has in the selector.
	 * @param info the information about the signature.
	 * @param isCheck generate the check or the non-checking version.
	 * */
	private static void generateTypedGetcall(final StringBuilder source, final int index, final procedureSignatureInfo info, final boolean isCheck) {
		final String functionName = isCheck ? "check_getcall" : "getcall";

		source.append(MessageFormat.format("public TitanAlt_Status {0}(final {1}_template getcall_template, final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", functionName, info.mJavaTypeName));
		source.append("if (procedure_queue.size() == 0) {\n");
		source.append("if(is_started) {\n");
		source.append("return TitanAlt_Status.ALT_MAYBE;\n");
		source.append("} else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("}\n");
		source.append("}\n");
		source.append("ProcedureQueueItem head = procedure_queue.getFirst();\n");
		source.append("if (!TitanBoolean.getNative(sender_template.match(head.sender_component, false))) {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (head.item_selection != proc_selection.CALL_{0}) '{'\n", index));
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(MessageFormat.format("'}' else if (!TitanBoolean.getNative(getcall_template.match_call(head.call_{0}, true))) '{'\n", index));
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append("} else {\n");
		source.append("//FIXME set param_ref and logging\n");
		if (!isCheck) {
			source.append("remove_proc_queue_head();\n");
		}
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}
}
