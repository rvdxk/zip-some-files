import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main extends JFrame {
    public Main()
    {
        this.setTitle("Zipper");
        this.setBounds(250,200,350,350);
        this.setJMenuBar(jMenuBar);

        JMenu menuFile = jMenuBar.add(new JMenu("Plik"));

        javax.swing.Action addAction = new Action("Dodaj", "Dodaj nowy wpis do archiwum.", "ctrl D", new ImageIcon("dodaj.jpg"));
        javax.swing.Action deleteAction = new Action("Usuń", "Usuń zaznaczony/zaznaczone wpis.", "ctrl U", new ImageIcon("usun.jpg"));
        javax.swing.Action zipAction = new Action("Zip", "Zippuj wybrane wpisy.", "ctrl Z");

        JMenuItem menuOpen = menuFile.add(addAction);
        JMenuItem menuDelete = menuFile.add(deleteAction);
        JMenuItem menuZip = menuFile.add(zipAction);

        buttonAdd = new JButton(addAction);
        buttonDelete = new JButton(deleteAction);
        buttonZip = new JButton(zipAction);
        JScrollPane scrollPane = new JScrollPane(list);


        list.setBorder(BorderFactory.createEtchedBorder());
        GroupLayout layout = new GroupLayout(this.getContentPane());

        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(scrollPane, 100, 150, Short.MAX_VALUE)
                        .addContainerGap(0,Short.MAX_VALUE)
                        .addGroup(
                              layout.createParallelGroup()
                                      .addComponent(buttonAdd)
                                      .addComponent(buttonDelete)
                                      .addComponent(buttonZip)
                        )
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonAdd)
                                .addComponent(buttonDelete)
                                        .addGap(5,40, Short.MAX_VALUE)
                                .addComponent(buttonZip))
        );


        this.getContentPane().setLayout(layout);
        this.setDefaultCloseOperation(3);

        this.pack();
    }

    private DefaultListModel listModel = new DefaultListModel()
    {
        @Override
        public void addElement (Object obj)
        {
            list.add(obj);
            super.addElement(((File)obj).getName());
        }
        public Object get(int index)
        {
            return list.get(index);
        }

        public Object remove(int index)
        {
            list.remove(index);
            return super.remove(index);
        }

        ArrayList list = new ArrayList();

    };
    private JList list = new JList(listModel);
    private JButton buttonAdd;
    private JButton buttonDelete;
    private JButton buttonZip;
    private JMenuBar jMenuBar = new JMenuBar();
    private JFileChooser chooser = new JFileChooser();


    public static void main(String[] args) {
        new Main().setVisible(true);

    }

    private class Action extends AbstractAction
    {

        public Action(String name, String description, String shortcut)
        {
            this.putValue(javax.swing.Action.NAME, name);
            this.putValue(javax.swing.Action.SHORT_DESCRIPTION, description);
            this.putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(shortcut));
        }
        public Action(String name, String description, String shortcut, Icon icon)
        {

            this(name, description, shortcut);
            this.putValue(javax.swing.Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
           if (e.getActionCommand().equals("Dodaj"))
               addToArchive();
           else if (e.getActionCommand().equals("Usuń"))
               deleteFromList();
           else if (e.getActionCommand().equals("Zip"))
               createZipArchive();

        }

        private void addToArchive()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);

            int tmp = chooser.showDialog(rootPane, "Dodaj do archiwum");
            if (tmp == JFileChooser.APPROVE_OPTION)
            {
                File[] path = chooser.getSelectedFiles();

                for (int i = 0; i < path.length; i++)
                    if (!isRepeat(path[i].getPath()))
                    listModel.addElement(path[i]);
            }

        }

        private boolean isRepeat(String testEntry)
        {
            for (int i = 0; i < listModel.getSize(); i++)
                if ( ((File) listModel.get(i)).getPath().equals(testEntry))
                    return true;

            return false;
        }

        private void deleteFromList()
        {
            int[] tmp = list.getSelectedIndices();

            for (int i = 0; i < tmp.length; i++)
                listModel.remove(tmp[i]- i);
        }

        private void createZipArchive()
        {
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "mojanazwa.zip"));
            int tmp = chooser.showDialog(rootPane, "Kompresuj");

            if (tmp == JFileChooser.APPROVE_OPTION) {
                byte tmpData[] = new byte[BUFFOR];

                try {
                    ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile()), BUFFOR));

                    for (int i = 0; i < listModel.getSize(); i++)
                    {
                        if (!((File) listModel.get(i)).isDirectory())
                            zip(zOutS, (File) listModel.get(i), tmpData, ((File) listModel.get(i)).getPath());
                        else
                        {
                            writePath((File) listModel.get(i));
                            for (int j = 0; j < pathList.size(); j++)
                                zip(zOutS, (File) pathList.get(j), tmpData, ((File) listModel.get(j)).getPath());

                            pathList.removeAll(pathList);
                        }

                    }
                    zOutS.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        private void zip(ZipOutputStream zOutS, File filePath, byte[] tmpData, String basePath) throws IOException
        {
            BufferedInputStream inS = new BufferedInputStream(new FileInputStream(filePath), BUFFOR);

            zOutS.putNextEntry(new ZipEntry(filePath.getPath().substring(basePath.lastIndexOf(File.separator) + 1)));

            int counter;

            while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1)
                zOutS.write(tmpData, 0, counter);

            zOutS.closeEntry();
            inS.close();
        }
        public static final int BUFFOR = 1024;

        private void writePath(File pathName)
        {
            String[] nameFileAndDirectories = pathName.list();

            for (int i = 0; i < nameFileAndDirectories.length; i++)
            {
                File p = new File(pathName.getPath(), nameFileAndDirectories[i]);
                        if(p.isFile())
                            pathList.add(p);

                            if (p.isDirectory())
                                writePath(new File(p.getPath()));
            }
        }

        ArrayList pathList = new ArrayList();
    }

}