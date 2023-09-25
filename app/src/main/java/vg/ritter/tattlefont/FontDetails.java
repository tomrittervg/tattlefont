package vg.ritter.tattlefont;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import vg.ritter.tattlefont.utility.IntTriple;


public class FontDetails implements Serializable {
    String Family = "";
    String SubFamily = "";
    String UniqueSubFamily = "";
    public String FullName = "";
    String FontVersion = "";
    int Revision = -1;
    long Created = -1;
    long Modified = -1;
    public String Path = "";
    public String Hash;
    String Source = "";

    FontDetails(String src, String path) throws Exception {
        this.Source = src;
        this.Path = path;
        this.Hash = calculateFileHash(this.Path);
        ReadFontFile();
    }

    @NonNull
    @Override
    public String toString() {
        return this.FullName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final FontDetails other = (FontDetails) obj;

        return this.Family.compareTo(other.Family) == 0 &&
                this.SubFamily.compareTo(other.SubFamily) == 0 &&
                this.UniqueSubFamily.compareTo(other.UniqueSubFamily) == 0 &&
                this.FullName.compareTo(other.FullName) == 0 &&
                this.FontVersion.compareTo(other.FontVersion) == 0 &&
                this.Revision == other.Revision;
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.Family, this.SubFamily, this.UniqueSubFamily, this.FullName, this.FontVersion, this.Revision);
    }

    private void ReadFontFile() throws IOException {
        DataInputStream file = new DataInputStream(new FileInputStream(this.Path));

        int numFonts = 1;
        int numTables;
        int magicNumber = file.readInt();
        int bytesReadSoFar = 4;

        if(magicNumber == 0x74746366) {
            // The Font File has a TTC Header
            int majorVersion = file.readUnsignedShort();
            file.skip(2);// Minor Version
            numFonts = file.readInt();
            bytesReadSoFar += 8;

            file.skip(4 * numFonts);// OffsetTable
            bytesReadSoFar += 4 * numFonts;
            if(majorVersion == 2) {
                file.skip(12);
                bytesReadSoFar += 12;
            }

            file.skip(4);//Magic Number for the Font
            bytesReadSoFar += 4;
        }
        numTables = file.readUnsignedShort();
        bytesReadSoFar += 2;
        file.skip(6); // Rest of header
        bytesReadSoFar += 6;

        // Find the head table
        int headOffset = 0;
        int nameOffset = 0, nameLength = 0;
        for (int i = 0; i < numTables; i++) {
            String tableName = new String(new char[]{(char) file.readUnsignedByte(), (char) file.readUnsignedByte(), (char) file.readUnsignedByte(), (char) file.readUnsignedByte()});
            file.skip(4); // checksum
            int offset = file.readInt(); // technically it's unsigned but we should be okay
            int length = file.readInt(); // technically it's unsigned but we should be okay

            bytesReadSoFar += 16;

            if (tableName.equals("head")) {
                headOffset = offset;
            } else if (tableName.equals("name")) {
                nameOffset = offset;
                nameLength = length;
            }
        }

        if(headOffset == 0 || nameOffset == 0) {
            throw new IOException("Could not find head or name table");
        }

        if(headOffset < nameOffset) {
            file.skip(headOffset - bytesReadSoFar);
            bytesReadSoFar = headOffset;
            bytesReadSoFar += ReadHeadTable(file);
            file.skip(nameOffset - bytesReadSoFar);
            ReadNameTable(file, nameLength);
        } else {
            file.skip(nameOffset - bytesReadSoFar);
            bytesReadSoFar = nameOffset;
            bytesReadSoFar += ReadNameTable(file, nameLength);
            file.skip(headOffset - bytesReadSoFar);
            ReadHeadTable(file);
        }
        file.close();
    }
    private int ReadHeadTable(DataInputStream file) throws IOException {
        // Find the details in the head table
        file.skip(4); // Fixed version
        this.Revision = file.readInt();
        file.skip(12); // checksum, magic, flags, units
        this.Created = file.readLong();
        this.Modified = file.readLong();
        return 36;
    }

    private int ReadNameTable(DataInputStream file, int tableLength) throws IOException {
        file.skip(2); // format
        int numNames = file.readUnsignedShort();
        int stringOffset = file.readUnsignedShort();

        int bytesReadSoFar = 6;

        List<IntTriple> nameTable = new ArrayList<>();
        for(int i=0; i<numNames; i++) {
            file.skip(6);//platform id, encoding id, langid
            int nameID = file.readUnsignedShort();
            int length = file.readUnsignedShort();
            int offset = file.readUnsignedShort();

            nameTable.add(new IntTriple(nameID, length, offset));
            bytesReadSoFar += 12;
        }

        byte[] stringTable = new byte[tableLength - bytesReadSoFar];
        if (stringTable.length != file.read(stringTable, 0, stringTable.length)) {
            throw new IOException("Did not read entire string table");
        }
        bytesReadSoFar += stringTable.length;

        // Now we're at the beginning of the string table
        for (int i=0; i<nameTable.size(); i++) {
            IntTriple e = nameTable.get(i);
            if(e.a == 1) {
                this.Family = getString(stringTable, e.c, e.b);
            } else if (e.a == 2) {
                this.SubFamily = getString(stringTable, e.c, e.b);
            } else if (e.a == 3) {
                this.UniqueSubFamily = getString(stringTable, e.c, e.b);
            } else if (e.a == 4) {
                this.FullName = getString(stringTable, e.c, e.b);
            } else if (e.a == 5) {
                this.FontVersion = getString(stringTable, e.c, e.b);
            }
        }

        return bytesReadSoFar;
    }

    private String getString(byte[] stringTable, int offset, int length) {
        return new String(Arrays.copyOfRange(stringTable, offset, offset + length));
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Family", this.Family.replaceAll("\u0000", ""));
            jsonObject.put("SubFamily", this.SubFamily.replaceAll("\u0000", ""));
            jsonObject.put("UniqueSubFamily", this.UniqueSubFamily.replaceAll("\u0000", ""));
            jsonObject.put("FullName", this.FullName.replaceAll("\u0000", ""));
            jsonObject.put("FontVersion", this.FontVersion.replaceAll("\u0000", ""));
            jsonObject.put("Revision", this.Revision);
            jsonObject.put("Created", this.Created);
            jsonObject.put("Modified", this.Modified);
            jsonObject.put("Path", this.Path.replaceAll("\u0000", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    private String calculateFileHash(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(filePath);

        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        byte[] digest = md.digest();

        // Convert the byte array to a hexadecimal string
        StringBuilder hashBuilder = new StringBuilder();
        for (byte b : digest) {
            hashBuilder.append(String.format("%02X", b));
        }

        fis.close();

        return hashBuilder.toString();
    }}
