package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * 协议数据包
 * 
 * @author hexianmao
 * @version 2008-8-14 下午07:29:53
 */
public class T4C8TTIproResponseDataPacket extends T4CTTIMsgPacket {

    byte           proSvrVer        = 6;
    short          oVersion         = -1;
    byte[]         proSvrStr        = "Linuxi386/Linux-2.0.34-8.1.0".getBytes();
    short          svrCharSet       = 0;
    byte           svrFlags         = 1;
    short          svrCharSetElem   = 0;
    boolean        svrInfoAvailable = false;

    byte[]         nchar_charset    = { 0x00, 0x0b, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07,
            (byte) 0xd0            };
    short          NCHAR_CHARSET    = 0;

    private int    i                = 0;
    private byte[] abyte0           = null;

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        if (msgCode != TTIPRO) {
            throw new RuntimeException("违反协议");
        }
        T4CPacketBuffer buffer = (T4CPacketBuffer) absbuffer;
        proSvrVer = buffer.unmarshalSB1();
        switch (proSvrVer) {
            case 4:
                oVersion = MIN_OVERSION_SUPPORTED;
                break;
            case 5:
                oVersion = ORACLE8_PROD_VERSION;
                break;
            case 6:
                oVersion = ORACLE81_PROD_VERSION;
                break;
            default:
                throw new RuntimeException("不支持从服务器接收到的 TTC 协议版本");
        }
        buffer.unmarshalSB1();
        proSvrStr = buffer.unmarshalTEXT(50);
        svrCharSet = (short) buffer.unmarshalUB2();
        svrFlags = (byte) buffer.unmarshalUB1();
        svrCharSetElem = (short) buffer.unmarshalUB2();
        if (svrCharSetElem > 0) {
            buffer.unmarshalNBytes(svrCharSetElem * 5);
        }
        svrInfoAvailable = true;

        if (proSvrVer < 5) {
            return;
        }
        byte byte0 = buffer.getRep((byte) 1);
        buffer.setRep((byte) 1, (byte) 0);
        i = buffer.unmarshalUB2();
        buffer.setRep((byte) 1, byte0);
        abyte0 = buffer.unmarshalNBytes(i);
        int j = 6 + (abyte0[5] & 0xff) + (abyte0[6] & 0xff);
        NCHAR_CHARSET = (short) ((abyte0[j + 3] & 0xff) << 8);
        NCHAR_CHARSET |= (short) (abyte0[j + 4] & 0xff);

        if (proSvrVer < 6) {
            return;
        }
        short word0 = buffer.unmarshalUB1();
        for (int k = 0; k < word0; k++) {
            buffer.unmarshalUB1();
        }
        word0 = buffer.unmarshalUB1();
        for (int l = 0; l < word0; l++) {
            buffer.unmarshalUB1();
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        msgCode = TTIPRO;
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.writeByte(proSvrVer);
        meg.marshalNULLPTR();
        meg.writeBytes(proSvrStr);
        meg.marshalNULLPTR();
        meg.marshalUB2(svrCharSet);
        meg.marshalUB1(svrFlags);
        meg.marshalUB2(svrCharSetElem);
        if (svrCharSetElem > 0) {
            byte[] ab = new byte[svrCharSetElem * 5];
            meg.marshalB1Array(ab);
        }

        if (proSvrVer < 5) {
            return;
        }
        byte byte0 = meg.getRep((byte) 1);
        meg.setRep((byte) 1, (byte) 0);
        meg.marshalUB2(i);
        meg.setRep((byte) 1, byte0);
        meg.marshalB1Array(abyte0);

        if (proSvrVer < 6) {
            return;
        }
        meg.marshalNULLPTR();
        meg.marshalNULLPTR();
    }

}
