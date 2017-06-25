package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.titan.designer.compiler.JavaGenData;

public class PortGenerator {
	public static class messageTypeInfo {
		/** Java type name of the field */
		private String mJavaTypeName;
		
		private String mJavaTemplateName;

		public messageTypeInfo(final String messageType, final String messageTemplate) {
			mJavaTypeName = messageType;
			mJavaTemplateName = messageTemplate;
		}
	}

	public static void generateClass(final JavaGenData aData, final StringBuilder source, final String genName, final ArrayList<messageTypeInfo> inMessages, final ArrayList<messageTypeInfo> outMessages) {
		aData.addImport("java.util.LinkedList");
		aData.addImport("java.text.MessageFormat");
		aData.addBuiltinTypeImport( "TitanPort" );
		aData.addBuiltinTypeImport( "TitanAlt_Status" );
		aData.addBuiltinTypeImport( "Base_Type" );

		source.append(MessageFormat.format("public static abstract class {0}_BASE extends TitanPort '{'\n", genName));
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

		source.append(MessageFormat.format("public {0}_BASE( final String portName) '{'\n", genName));
		source.append("super(portName);\n");
		source.append("}\n\n");

		source.append("private void remove_msg_queue_head() {\n");
		source.append("message_queue.removeFirst();\n");
		source.append("}\n\n");

		for (int i = 0 ; i < outMessages.size(); i++) {
			messageTypeInfo outType = outMessages.get(i);

			source.append(MessageFormat.format("public void send(final {0} send_par, final TitanComponent destination_component) '{'\n", outType.mJavaTypeName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Sending a message on port {0}, which is not started.\", getName()));\n");
			source.append("}\n");
			source.append("if (!destination_component.isBound()) {\n");
			source.append("throw new TtcnError(\"Unbound component reference in the to clause of send operation.\");\n");
			source.append("}\n");
			source.append("//FIXME logging\n");
			source.append("if (TitanBoolean.getNative(destination_component.operatorEquals(TitanComponent.SYSTEM_COMPREF))) {\n");
			source.append("//FIXME get_default_destination\n");
			source.append("outgoing_send(send_par);\n");
			source.append("} else {\n");
			source.append("//FIXME implement\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Sending messages on port {0}, is not yet supported.\", getName()));\n");
			source.append("}\n");
			source.append("}\n\n");
		}

		// outgoing send functions
		for (int i = 0 ; i < outMessages.size(); i++) {
			messageTypeInfo outType = outMessages.get(i);

			source.append(MessageFormat.format("public abstract void outgoing_send(final {0} send_par);\n\n", outType.mJavaTypeName));
		}

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
		source.append("} else if (!sender_template.match(my_head.sender_component, false).getValue()) {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");

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
		source.append("} else if (!sender_template.match(my_head.sender_component, false).getValue()) {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_NO;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");

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
		source.append("} else if (!sender_template.match(my_head.sender_component, false).getValue()) {\n");
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_REPEAT;\n");
		source.append(" } else {\n");
		source.append("//FIXME logging\n");
		source.append("remove_msg_queue_head();\n");
		source.append("return TitanAlt_Status.ALT_YES;\n");
		source.append("}\n");
		source.append("}\n\n");

		// generic and simplified receive for experimentation
		for (int i = 0 ; i < inMessages.size(); i++) {
			messageTypeInfo inType = inMessages.get(i);
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
			source.append("} else if (!sender_template.match(my_head.sender_component, false).getValue()) {\n");
			source.append("//FIXME logging\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0}) '{'\n", i));
			source.append("//FIXME logging\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(MessageFormat.format("'}' else if (!(my_head.message instanceof {0})) '{'\n", typeValueName));
			source.append("//FIXME report error \n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(MessageFormat.format("'}' else if (!value_template.match(({0}) my_head.message).getValue()) '{'\n", typeValueName));
			source.append("//FIXME implement\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(" } else {\n");
			source.append("//FIXME implement, right now we just assume perfect match\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_YES;\n");
			source.append("}\n");
			source.append("}\n\n");

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
			source.append("} else if (!sender_template.match(my_head.sender_component, false).getValue()) {\n");
			source.append("//FIXME logging\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0}) '{'\n", i));
			source.append("//FIXME logging\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(MessageFormat.format("'}' else if (!(my_head.message instanceof {0})) '{'\n", typeValueName));
			source.append("//FIXME report error \n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(MessageFormat.format("'}' else if (!value_template.match(({0}) my_head.message).getValue()) '{'\n", typeValueName));
			source.append("//FIXME implement\n");
			source.append("return TitanAlt_Status.ALT_NO;\n");
			source.append(" } else {\n");
			source.append("//FIXME implement, right now we just assume perfect match\n");
			source.append("return TitanAlt_Status.ALT_YES;\n");
			source.append("}\n");
			source.append("}\n\n");

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
			source.append("} else if (!sender_template.match(my_head.sender_component, false).getValue()) {\n");
			source.append("//FIXME logging\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_REPEAT;\n");
			source.append(MessageFormat.format("} else if (my_head.item_selection != message_selection.MESSAGE_{0}) '{'\n", i));
			source.append("//FIXME logging\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_REPEAT;\n");
			source.append(MessageFormat.format("'}' else if (!(my_head.message instanceof {0})) '{'\n", typeValueName));
			source.append("//FIXME logging\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_REPEAT;\n");
			source.append(MessageFormat.format("'}' else if (!value_template.match(({0}) my_head.message).getValue()) '{'\n", typeValueName));
			source.append("//FIXME logging\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_REPEAT;\n");
			source.append(" } else {\n");
			source.append("//FIXME implement, right now we just assume perfect match\n");
			source.append("remove_msg_queue_head();\n");
			source.append("return TitanAlt_Status.ALT_YES;\n");
			source.append("}\n");
			source.append("}\n\n");

			source.append(MessageFormat.format("private void incoming_message(final {0} incoming_par, final int sender_component) '{'\n", typeValueName));
			source.append("if (!is_started) {\n");
			source.append("throw new TtcnError(MessageFormat.format(\"Port {0} is not started but a message has arrived on it.\", getName()));\n");
			source.append("}\n");
			source.append("MessageQueueItem new_item = new MessageQueueItem();\n");
			source.append(MessageFormat.format("new_item.item_selection = message_selection.MESSAGE_{0};\n", i));
			source.append(MessageFormat.format("new_item.message = new {0}(incoming_par);\n", typeValueName));
			source.append("new_item.sender_component = sender_component;\n");
			source.append("message_queue.addLast(new_item);\n");
			source.append("}\n\n");

			source.append(MessageFormat.format("protected void incoming_message(final {0} incoming_par) '{'\n", typeValueName));
			source.append("incoming_message(incoming_par, TitanComponent.SYSTEM_COMPREF);\n");
			source.append("}\n\n");
		}


		source.append( "//TODO: port code generation is not yet fully implemented!\n" );

		source.append("}\n\n");
	}
}
