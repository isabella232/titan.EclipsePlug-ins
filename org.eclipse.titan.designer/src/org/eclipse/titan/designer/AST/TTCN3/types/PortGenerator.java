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
	 * originally port_msg_type_tag is something like this
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

	/**
	 * This function can be used to generate the class of port types
	 *
	 * defPortClass in compiler2/port.{h,c}
	 *
	 * @param aData: only used to update imports if needed.
	 * @param source: where the source code is to be generated.
	 * @param genName: the name of the generated class representing the port type.
	 * @param inMessages: the list of information about the incoming messages.
	 * @param outMessages: the list of information about the outgoing messages.
	 * */
	public static void generateClass(final JavaGenData aData, final StringBuilder source, final String genName, final ArrayList<messageTypeInfo> inMessages, final ArrayList<messageTypeInfo> outMessages, final TestportType testportType) {
		aData.addImport("java.util.LinkedList");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport( "TitanPort" );
		aData.addBuiltinTypeImport( "TitanAlt_Status" );
		aData.addBuiltinTypeImport( "Base_Type" );

		generateDeclaration(source, genName, inMessages, testportType);

		source.append("private void remove_msg_queue_head() {\n");
		source.append("message_queue.removeFirst();\n");
		source.append("}\n\n");

		for (int i = 0 ; i < outMessages.size(); i++) {
			messageTypeInfo outType = outMessages.get(i);

			generateSend(source, outType, testportType);
		}

		generateGenericReceive(source);
		generateGenericCheckReceive(source);
		generateGenericTrigger(source);

		// generic and simplified receive for experimentation
		for (int i = 0 ; i < inMessages.size(); i++) {
			messageTypeInfo inType = inMessages.get(i);

			generateTypedReceive(source, i, inType);
			generateTypedCheckReceive(source, i, inType);
			generateTypeTrigger(source, i, inType);
			generateTypedIncomminMessage(source, i, inType, testportType);
		}

		source.append( "//TODO: port code generation is not yet fully implemented!\n" );

		source.append("}\n\n");
	}

	/**
	 * This function generates the declaration of the generated port type class.
	 *
	 * @param source: where the source code is to be generated.
	 * @param inMessages: the list of information about the incoming messages.
	 * */
	private static void generateDeclaration(final StringBuilder source, final String genName, final ArrayList<messageTypeInfo> inMessages, final TestportType testportType) {
		String className;
		String baseClassName;
		if (testportType == TestportType.INTERNAL) {
			className = genName;
			baseClassName = "TitanPort";
		} else {
			// FIXME more complicated
			className = genName + "_BASE";
			baseClassName = "TitanPort";
		}
		source.append(MessageFormat.format("public static class {0} extends {1} '{'\n", className, baseClassName));

		source.append("enum message_selection { ");
		for (int i = 0 ; i < inMessages.size(); i++) {
			if (i > 0) {
				source.append(", ");
			}
			source.append(MessageFormat.format("MESSAGE_{0}", i));
		}
		source.append("};\n");

		source.append("private class MessageQueueItem {\n");
		source.append("message_selection item_selection;\n");
		source.append("Base_Type message;\n");
		source.append("int sender_component;\n");
		source.append("}\n");

		source.append("private LinkedList<MessageQueueItem> message_queue = new LinkedList<>();\n\n");

		source.append(MessageFormat.format("public {0}( final String portName) '{'\n", className));
		source.append("super(portName);\n");
		source.append("}\n\n");

		//FIXME more complicated conditional
		if (testportType == TestportType.INTERNAL) {
			source.append(MessageFormat.format("public {0}( ) '{'\n", className));
			source.append(MessageFormat.format("this((String)null);\n", className));
			source.append("}\n\n");
		}
	}

	/**
	 * This function generates the sending functions.
	 *
	 * @param source: where the source code is to be generated.
	 * @param outType: the information about the outgoing message.
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
	 * This function generates the generic receive function.
	 *
	 * @param source: where the source code is to be generated.
	 * */
	private static void generateGenericReceive(final StringBuilder source) {
		source.append("public TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {\n");
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
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the generic check_receive or in TTCN-3 check(receive) function.
	 *
	 * @param source: where the source code is to be generated.
	 * */
	private static void generateGenericCheckReceive(final StringBuilder source) {
		source.append("public TitanAlt_Status check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {\n");
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
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the generic trigger function.
	 *
	 * @param source: where the source code is to be generated.
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
	 * This function generates the receive function for a type
	 *
	 * @param source: where the source code is to be generated.
	 * @param index: the index this message type has in the declaration the port type.
	 * @param inType: the information about the incoming message.
	 * */
	private static void generateTypedReceive(final StringBuilder source, final int index, final messageTypeInfo inType) {
		String typeValueName = inType.mJavaTypeName;
		String typeTemplateName = inType.mJavaTemplateName;

		//FIXME there are actually more parameters
		source.append(MessageFormat.format("public TitanAlt_Status receive(final {0} value_template, final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", typeTemplateName));
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
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the check_receive or in TTCN-3 check(receive) function for a type
	 *
	 * @param source: where the source code is to be generated.
	 * @param index: the index this message type has in the declaration the port type.
	 * @param inType: the information about the incoming message.
	 * */
	private static void generateTypedCheckReceive(final StringBuilder source, final int index, final messageTypeInfo inType) {
		String typeValueName = inType.mJavaTypeName;
		String typeTemplateName = inType.mJavaTemplateName;

		//FIXME there are actually more parameters
		source.append(MessageFormat.format("public TitanAlt_Status check_receive(final {0} value_template, final TitanComponent_template sender_template, final TitanComponent sender_pointer) '{'\n", typeTemplateName));
		source.append("if (value_template.getSelection() == template_sel.ANY_OR_OMIT) {\n");
		source.append("throw new TtcnError(\"Check-receive operation using '*' as matching template\");\n");
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
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");
	}

	/**
	 * This function generates the trigger function for a type
	 *
	 * @param source: where the source code is to be generated.
	 * @param index: the index this message type has in the declaration the port type.
	 * @param inType: the information about the incoming message.
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
	 * @param source: where the source code is to be generated.
	 * @param index: the index this message type has in the declaration the port type.
	 * @param inType: the information about the incoming message.
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
}
